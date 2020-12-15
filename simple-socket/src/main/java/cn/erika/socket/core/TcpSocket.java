package cn.erika.socket.core;

import cn.erika.aop.exception.BeanException;
import cn.erika.config.Constant;
import cn.erika.socket.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 作为Socket的包装类 用于增强Socket的功能
 * 需要一个TcpHandler提供功能支持
 */
public class TcpSocket implements BaseSocket, Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    // 持有的Socket对象 用来响应请求
    private Socket socket;
    // 持有的Reader对象 用来解析数据
    private TcpReader reader;
    // 持有的Handler对象 在连接建立后初始化连接属性和处理连接后的动作
    private Handler handler;
    private Charset charset;
    // 记录连接的属性
    private Map<String, Object> attr = new HashMap<>();

    private InputStream in;
    private OutputStream out;

    public TcpSocket(Socket socket, Handler handler, Charset charset) throws IOException {
        set(Constant.TYPE, Constant.SERVER);
        this.handler = handler;
        this.charset = charset;
        this.reader = new TcpReader(charset);
        this.socket = socket;
        onEstablished();
    }

    public TcpSocket(SocketAddress address, Handler handler, Charset charset) throws IOException {
        set(Constant.TYPE, Constant.CLIENT);
        this.handler = handler;
        this.charset = charset;
        this.reader = new TcpReader(charset);
        this.socket = new Socket();
        this.socket.setReuseAddress(true);
        this.socket.connect(address);
        onEstablished();
    }

    private void onEstablished() throws IOException {
        set(Constant.LINK_TIME, new Date());
        handler.init(this);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
        try {
            handler.onOpen(this);
        } catch (BeanException e) {
            handler.onError(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        // 缓冲区大小从创建的socket中获取（接收区的大小）
        try {
            int cacheSize = this.socket.getReceiveBufferSize();
            byte[] cache = new byte[cacheSize];
            int len;
            try {
                while (!socket.isClosed() && (len = in.read(cache)) > -1) {
                    // 向处理器传输缓冲区和有效字节数
                    reader.read(this, cache, len);
                }
            } catch (IOException e) {
                handler.onClose(this);
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
            send(info.toString().getBytes(charset));
            send(info.getData());
        } catch (Exception e) {
            handler.onError(e.getMessage(), e);
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
    public SocketAddress getRemoteAddress() {
        return this.socket.getRemoteSocketAddress();
    }

    @Override
    public boolean isClosed() {
        return this.socket.isClosed();
    }

    @Override
    public void close() {
        try {
            if (!socket.isClosed()) {
                SocketAddress address = socket.getRemoteSocketAddress();
                socket.close();
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
