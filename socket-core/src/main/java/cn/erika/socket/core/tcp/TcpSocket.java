package cn.erika.socket.core.tcp;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.core.Handler;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.Reader;
import cn.erika.socket.exception.DataFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;

public class TcpSocket extends BaseSocket implements Runnable {
    private Socket socket;
    private Reader reader;

    private InputStream in;
    private OutputStream out;

    private static int count = 0;

    public TcpSocket(Socket socket, Handler handler) throws IOException {
        set(Constant.TYPE, Constant.SERVER);
        this.socket = socket;
        this.handler = handler;
        init();
    }

    public TcpSocket(ISocket socket, Handler handler) throws IOException {
        set(Constant.TYPE, Constant.CLIENT);
        set(Constant.PARENT_SOCKET, socket);
        this.socket = new Socket();
        this.handler = handler;
        this.socket.setReuseAddress(true);
        try {
            this.socket.connect(socket.getRemoteAddress());
            init();
        } catch (ConnectException e) {
            throw new IOException("无法连接到服务器");
        }
    }

    public TcpSocket(SocketAddress address, Handler handler) throws IOException {
        set(Constant.TYPE, Constant.CLIENT);
        this.socket = new Socket();
        this.handler = handler;
        this.socket.setReuseAddress(true);
        try {
            this.socket.connect(address);
            init();
        } catch (ConnectException e) {
            throw new IOException("无法连接到服务器");
        }
    }

    private void init() throws IOException {
        set(Constant.LINK_TIME, new Date());
        this.socket.setTcpNoDelay(true);
        this.charset = GlobalSettings.charset;
        this.reader = new TcpReader();
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        Thread thread = new Thread(this, this.getClass().getSimpleName()
                + String.format("-%5s", count++).replaceAll("\\s","0"));
        thread.setDaemon(true);
        thread.start();
        handler.init(this);
    }

    @Override
    public void run() {
        try {
            int cacheSize = this.socket.getReceiveBufferSize();
            byte[] cache = new byte[cacheSize];
            int len;
            while (!socket.isClosed() && (len = in.read(cache)) > -1) {
                // 向处理器传输缓冲区和有效字节数
                reader.read(this, cache, len);
            }
        } catch (IOException e) {
            log.warn("关闭连接: " + get(Constant.UID));
        } catch (DataFormatException e) {
            log.error(e.getMessage());
            close();
        }
    }

    @Override
    public void send(byte[] data) throws IOException {
        int pos = 0;
        int len = data.length;
        // 这里用pos标记发送数据的长度 每次发送缓冲区大小个字节 直到pos等于数据长度len
        // 缓冲区的大小从socket获取（发送区的大小）
        int cacheSize = socket.getSendBufferSize();
        while (len - pos > cacheSize) {
            out.write(data, pos, cacheSize);
            pos += cacheSize;
        }
        out.write(data, pos, len - pos);
        out.flush();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return socket.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return socket.getLocalSocketAddress();
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            log.error("关闭连接的过程中发生错误: " + e.getMessage());
        } finally {
            handler.onClose(this);
        }
    }
}
