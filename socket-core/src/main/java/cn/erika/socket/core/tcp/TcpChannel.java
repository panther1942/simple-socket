package cn.erika.socket.core.tcp;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.core.Handler;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.exception.DataFormatException;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class TcpChannel extends BaseSocket {
    private TcpReader reader;

    private SocketChannel channel;
    private Selector selector;

    public TcpChannel(SocketChannel channel, Handler handler, Selector selector) throws IOException {
        set(Constant.TYPE, Constant.SERVER);
        this.handler = handler;
        this.charset = GlobalSettings.charset;
        this.reader = new TcpReader(charset);
        this.selector = selector;
        this.channel = channel;
        this.channel.configureBlocking(false);
        this.channel.register(selector, SelectionKey.OP_READ);
        init();
    }

    public TcpChannel(SocketAddress address, Handler handler, Selector selector) throws IOException {
        set(Constant.TYPE, Constant.CLIENT);
        this.handler = handler;
        this.charset = GlobalSettings.charset;
        this.reader = new TcpReader(charset);
        this.selector = selector;
        this.channel = SocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.register(selector, SelectionKey.OP_CONNECT);
        this.channel.connect(address);
        init();
    }

    private void init() throws IOException {
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
        } catch (IOException e) {
            log.warn("关闭连接: " + get(Constant.UID));
        } catch (DataFormatException e) {
            log.error(e.getMessage());
            close();
        }
    }

    @Override
    public synchronized void send(byte[] data) throws IOException {
        if (!isClosed()) {
            channel.write(ByteBuffer.wrap(data));
            selector.wakeup();
            channel.register(selector, SelectionKey.OP_READ);
        } else {
            throw new IOException("没有连接到服务器");
        }
    }

    @Override
    public SocketAddress getRemoteAddress() {
        try {
            return channel.getRemoteAddress();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public SocketAddress getLocalAddress() {
        try {
            return channel.getLocalAddress();
        } catch (IOException e) {
            return null;
        }
    }


    @Override
    public boolean isClosed() {
        return !this.channel.isOpen();
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            log.error("关闭连接的过程中发生错误: " + e.getMessage());
        } finally {
            handler.onClose(this);
        }
    }
}
