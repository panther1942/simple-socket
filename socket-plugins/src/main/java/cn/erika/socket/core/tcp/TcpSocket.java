package cn.erika.socket.core.tcp;

import cn.erika.socket.config.Constant;
import cn.erika.socket.config.GlobalSettings;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.Handler;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.core.component.DataInfo;
import cn.erika.socket.exception.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Date;

public class TcpSocket extends Socket implements Runnable {
    private java.net.Socket socket;
    private TcpReader reader;
    private Handler handler;
    private Charset charset;

    private InputStream in;
    private OutputStream out;

    public TcpSocket(java.net.Socket socket, Handler handler) throws IOException {
        set(Constant.TYPE, Constant.SERVER);
        this.socket = socket;
        this.handler = handler;
        init();
    }

    public TcpSocket(SocketAddress address, Handler handler) throws IOException {
        set(Constant.TYPE, Constant.CLIENT);
        this.socket = new java.net.Socket();
        this.handler = handler;
        this.socket.setReuseAddress(true);
        this.socket.connect(address);
        init();
    }

    private void init() throws IOException {
        set(Constant.LINK_TIME, new Date());
        this.charset = GlobalSettings.charset;
        this.reader = new TcpReader(charset);
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        Thread thread = new Thread(this);
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
            close();
        } catch (IOException e) {
            handler.onError(this, e);
        }
    }

    private synchronized void send(byte[] data) throws IOException {
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
    public void send(DataInfo info) {
        try {
            send(info.toString().getBytes(charset));
            send(info.getData());
        } catch (IOException e) {
            handler.onError(this, e);
        }
    }

    @Override
    public void receive(Message message) {
        try {
            handler.onMessage(this, message);
        } catch (ServiceException e) {
            System.err.println(e.getMessage());
            close();
        }
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭连接的过程中发生错误: " + e.getMessage());
        }
    }
}
