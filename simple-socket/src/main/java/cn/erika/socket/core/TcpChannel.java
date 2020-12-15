package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.socket.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TcpChannel implements BaseSocket {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private SocketChannel channel;
    private Handler handler;
    private Charset charset;
    private TcpReader reader;
    private Selector selector;
    private Map<String, Object> attr = new HashMap<>();

    public TcpChannel(SocketChannel channel, Handler handler, Selector selector, Charset charset) throws IOException {
        set(Constant.TYPE, Constant.SERVER);
        this.handler = handler;
        this.selector = selector;
        this.charset = charset;
        this.reader = new TcpReader(charset);
        this.channel = channel;
        this.channel.configureBlocking(false);
        this.channel.register(selector, SelectionKey.OP_READ);
        onEstablished();
    }

    public TcpChannel(InetSocketAddress address, Handler handler, Selector selector, Charset charset) throws IOException {
        set(Constant.TYPE, Constant.CLIENT);
        this.handler = handler;
        this.selector = selector;
        this.charset = charset;
        this.reader = new TcpReader(charset);
        this.channel = SocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.register(selector, SelectionKey.OP_CONNECT);
        this.channel.connect(address);
        onEstablished();
    }

    private void onEstablished() throws IOException {
        set(Constant.LINK_TIME, new Date());
        handler.init(this);
    }

    public void read() throws IOException {
        try {
            int cacheSize = channel.socket().getReceiveBufferSize();
            ByteBuffer buffer = ByteBuffer.allocate(cacheSize);
            if (channel.read(buffer) > 0) {
                buffer.flip();
                byte[] data = buffer.array();
                int len = buffer.limit();
                reader.read(this, data, len);
            }
        } catch (SocketException e) {
            handler.onError(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void send(Message message) {
        try {
            DataInfo info = Processor.beforeSend(this, message);
            byte[] bInfo = info.toString().getBytes(charset);
            byte[] bData = info.getData();
            byte[] data = new byte[bInfo.length + bData.length];
            System.arraycopy(bInfo, 0, data, 0, bInfo.length);
            System.arraycopy(bData, 0, data, bInfo.length, bData.length);
            channel.write(ByteBuffer.wrap(data));
            selector.wakeup();
            channel.register(selector, SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            handler.onClose(this);
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
        return !this.channel.isOpen();
    }

    @Override
    public void close() {
        try {
            if (channel.isOpen()) {
                SocketAddress address = channel.getRemoteAddress();
                channel.close();
                log.info("关闭连接: [" + address + "]");
            }
        } catch (IOException e) {
            log.warn(e.getMessage());
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
}
