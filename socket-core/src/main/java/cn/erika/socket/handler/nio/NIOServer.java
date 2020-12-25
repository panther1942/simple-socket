package cn.erika.socket.handler.nio;

import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.tcp.TcpChannel;
import cn.erika.socket.handler.BaseServer;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class NIOServer extends BaseServer implements Runnable, IServer {
    private ServerSocketChannel server;
    private Selector selector = Selector.open();
    private ConcurrentHashMap<SocketChannel, TcpChannel> channelMap = new ConcurrentHashMap<>();

    public NIOServer(SocketAddress address) throws IOException {
        this.server = ServerSocketChannel.open();
        this.server.configureBlocking(false);
        this.server.bind(address);
        this.server.register(selector, SelectionKey.OP_ACCEPT);
        log.info("服务器监听端口: " + server.getLocalAddress());
    }

    @Override
    public void run() {
        while (server.isOpen()) {
            try {
                int events = selector.select();
                if (events > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        if (key.isAcceptable()) {
                            try {
                                SocketChannel sc = server.accept();
                                sc.configureBlocking(false);
                                sc.register(selector, SelectionKey.OP_READ);
                                TcpChannel channel = new TcpChannel(sc, this, selector);
                                channelMap.put(sc, channel);
                            } catch (IOException e) {
                                // 如果这里出现异常 就是新连接接入的时候出现异常 应该要断开连接
                                log.error(e.getMessage());
                            }
                        } else {
                            SocketChannel sc = (SocketChannel) key.channel();
                            TcpChannel channel = channelMap.get(sc);
                            try {
                                channel.read();
                            } catch (IOException e) {
                                // 这里是读取数据的时候发生异常 需要更细致的处理
                                // 比如判断是读取出错还是连接断开
                                close(channel);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void close(ISocket socket) {
        socket.close();
    }

    @Override
    public void close() {
        super.close();
        try {
            if (server.isOpen()) {
                server.close();
                log.info("关闭服务器");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public boolean isClosed() {
        return !server.isOpen();
    }

    @Override
    public SocketAddress getLocalAddress() {
        try {
            return server.getLocalAddress();
        } catch (IOException e) {
            return null;
        }
    }
}
