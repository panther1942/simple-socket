package cn.erika.socket.core;

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
public class TcpSocket implements Runnable {
    // 持有的Socket对象 用来响应请求
    private Socket socket;
    // 持有的Reader对象 用来解析数据
    private Reader reader;
    // 持有的Handler对象 在连接建立后初始化连接属性和处理连接后的动作
    private Handler handler;
    // 记录连接开始的时间
    private Date startTime;
    // 记录连接的属性
    private Map<String, Object> attr = new HashMap<>();

    private InputStream in;
    private OutputStream out;

    public TcpSocket(Socket socket, Handler handler, Charset charset) throws IOException {
        this.handler = handler;
        this.reader = new Reader(handler, charset);
        this.socket = socket;
        onEstablished();
    }

    public TcpSocket(SocketAddress address, Handler handler, Charset charset) throws IOException {
        this.handler = handler;
        this.reader = new Reader(handler, charset);
        this.socket = new Socket();
        this.socket.setReuseAddress(true);
        this.socket.connect(address);
        onEstablished();
    }

    private void onEstablished() throws IOException {
        startTime = new Date();
        handler.init(this);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
        handler.onOpen(this);
    }

    @Override
    public void run() {
        // 缓冲区 如果初始化中未设置缓冲区大小将使用默认值4096 即4k
        try {
            int cacheSize = this.socket.getReceiveBufferSize();
            SocketAddress address = this.socket.getRemoteSocketAddress();
            byte[] cache = new byte[cacheSize];
            int len;
            try {
                while (!socket.isClosed() && (len = in.read(cache)) > -1) {
                    // 向处理器传输缓冲区和有效字节数
                    reader.read(this, cache, len);
                }
            } catch (IOException e) {
                handler.onError("连接中断 From: " + address.toString(), e);
            } finally {
                try {
                    close();
                } catch (IOException e) {
                    handler.onError("断开连接的过程中发生错误", e);
                }
            }
        } catch (SocketException e) {
            handler.onError(e.getMessage(), e);
        }
    }

    /**
     * 实现了Socket的写方法 并尝试解决2G以上字节流的问题(int最大值为2G)
     *
     * @param data 要发送的数据 是否编码由具体实现决定
     * @param len  发送数据的实际长度
     * @throws IOException 如果传输过程发生错误
     */
    private void write(byte[] data, int len) throws IOException {
        int pos = 0;
        // 这里用pos标记发送数据的长度 每次发送缓冲区大小个字节 直到pos等于数据长度len
        int cacheSize = socket.getSendBufferSize();
        while (len - pos > cacheSize) {
            out.write(data, pos, cacheSize);
            pos += cacheSize;
        }
        out.write(data, pos, len - pos);
        out.flush();
    }

    public void send(byte[] data) throws IOException {
        DataInfo info = new DataInfo();
        info.setPos(0);
        info.setLen(data.length);
        info.setTimestamp(new Date());
        if (!socket.isClosed()) {
            write(info.toString().getBytes(), info.toString().length());
            write(data, data.length);
        } else {
            throw new IOException("连接已被关闭: " + socket.getRemoteSocketAddress());
        }
    }

    public void close() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * 获取包装的Socket对象 方便直接调用Socket的底层方法
     *
     * @return 包含的Socket对象
     */
    public Socket getSocket() {
        return this.socket;
    }

    // 获取连接开始时间
    public Date linkTime() {
        return this.startTime;
    }

    // 设置连接额外属性
    @SuppressWarnings("unchecked")
    public <T> T set(String k, Object v) {
        return (T) this.attr.put(k, v);
    }

    // 获取连接额外属性
    @SuppressWarnings("unchecked")
    public <T> T get(String k) {
        return (T) this.attr.get(k);
    }

    // 移除连接额外属性
    @SuppressWarnings("unchecked")
    public <T> T remove(String k) {
        return (T) this.attr.remove(k);
    }


}
