package cn.erika.socket.handler.impl;

import cn.erika.socket.core.TcpChannel;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class NIOServer extends AbstractServerHandler implements IServer, Runnable {
    private ServerSocketChannel server;
    private Selector selector = Selector.open();
    private ConcurrentHashMap<SocketChannel, TcpChannel> channelMap = new ConcurrentHashMap<>();

    public NIOServer(InetSocketAddress address) throws IOException {
        this.linkManager = new LinkManager();
        this.server = ServerSocketChannel.open();
        this.server.configureBlocking(false);
        this.server.bind(address);
        this.server.register(selector, SelectionKey.OP_ACCEPT);
        log.info("Listen: " + address.getAddress());
    }

    @Override
    public void run() {
        while (true) {
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
                                TcpChannel channel = new TcpChannel(sc, this, selector, CHARSET);
                                channelMap.put(sc, channel);
                                onOpen(channel);
                            } catch (IOException e) {
                                log.error(e.getMessage());
                            }
                        } else {
                            SocketChannel sc = (SocketChannel) key.channel();
                            TcpChannel channel = channelMap.get(sc);
                            try {
                                channel.read();
                            } catch (IOException e) {
                                close(channel);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                onError(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isClosed() {
        return !server.isOpen();
    }

    @Override
    public void close() {
        try {
            if (server.isOpen()) {
                server.close();
                log.info("关闭服务器");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
