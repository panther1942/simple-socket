package cn.erika.socket.core;

import cn.erika.aop.exception.BeanException;
import cn.erika.config.Constant;
import cn.erika.socket.common.component.*;
import cn.erika.util.compress.CompressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TcpChannel implements BaseChannel {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private SocketChannel channel;
    private TcpReader reader;
    private Handler handler;
    private Charset charset;
    private Map<String, Object> attr = new HashMap<>();

        public TcpChannel(SocketChannel channel, Handler handler, Charset charset) throws IOException {
        this.handler = handler;
        this.charset = charset;
        this.reader = new TcpReader(charset);
        this.channel = channel;
        this.channel.configureBlocking(false);
        onEstablished();
    }

    public TcpChannel(InetSocketAddress address, Handler handler, Charset charset) throws IOException {
        this.handler = handler;
        this.charset = charset;
        this.reader = new TcpReader(charset);
        this.channel = SocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.connect(address);
        if (this.channel.isConnectionPending()) {
            this.channel.finishConnect();
        }
        onEstablished();
    }

    private void onEstablished() throws IOException {
        set(Constant.LINK_TIME, new Date());
        handler.init(this);
        try {
            handler.onOpen(this);
        } catch (BeanException e) {
            handler.onError(e.getMessage(), e);
        }
    }

    private void write(byte[] data) throws IOException {
        channel.write(ByteBuffer.wrap(data));
    }

    public void read(){
        try {
            int cacheSize = channel.socket().getReceiveBufferSize();
            ByteBuffer buffer = ByteBuffer.allocate(cacheSize);
            try {
                if (channel.read(buffer) > 0) {
                    buffer.flip();
                    byte[] data = buffer.array();
                    if (data.length > 0) {
                        reader.read(this, data, data.length);
                    }
                }
            } catch (IOException e) {
                handler.onError("连接中断", e);
            } catch (CompressException e) {
                handler.onError(e.getMessage(), e);
            } finally {
                close();
            }
        } catch (SocketException e) {
            handler.onError(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void send(Message message) {
        try {
            DataInfo info = Processor.beforeSend(this, message);
            write(info.toString().getBytes(charset));
            write(info.getData());
        } catch (Exception e) {
            handler.onError(e.getMessage(), e);
        }
    }

    @Override
    public void receive(DataInfo info, byte[] data) {
        try {
            Message message = Processor.beforeRead(this, info, data);
            handler.onMessage(this, info, message);
        } catch (Exception e) {
            handler.onError(e.getMessage(), e);
        }
    }

    @Override
    public void ready() {
        handler.onReady(this);
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return this.channel.getRemoteAddress();
    }

    @Override
    public boolean isClosed() {
        return !this.channel.isConnected();
    }

    @Override
    public void close() {
        try {
            if (channel.isConnected()) {
                channel.close();
            }
        } catch (IOException e) {
            handler.onError(e.getMessage(), e);
        }
    }

    // 设置连接额外属性
    @SuppressWarnings("unchecked")
    @Override
    public <T> T set(String k, Object v) {
        return (T) this.attr.put(k, v);
    }

    // 获取连接额外属性
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String k) {
        return (T) this.attr.get(k);
    }

    // 移除连接额外属性
    @SuppressWarnings("unchecked")
    @Override
    public <T> T remove(String k) {
        return (T) this.attr.remove(k);
    }

    @Override
    public void register(Selector selector, int selectorStatus) throws ClosedChannelException {
        this.channel.register(selector, selectorStatus);
    }
}
