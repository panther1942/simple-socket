package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.config.Constant;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.core.TcpChannel;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NIOServer extends AbstractServerHandler implements IServer, Runnable {
    private ServerSocketChannel server;
    private Selector selector = Selector.open();
    private Map<SocketAddress, TcpChannel> channelMap = new HashMap<>();

    public NIOServer(InetSocketAddress address) throws IOException {
        this.linkManager = new LinkManager();
        this.server = ServerSocketChannel.open();
        this.server.configureBlocking(false);
        this.server.bind(address);
        this.server.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        while (this.server.isOpen()) {
            try {
                int events = selector.select();
                if (events > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        try {
                            if (key.isAcceptable()) {
                                SocketChannel sc = server.accept();
                                TcpChannel channel = new TcpChannel(sc, this, CHARSET);
                                channel.register(selector, SelectionKey.OP_READ);
                                channelMap.put(sc.getRemoteAddress(), channel);
                            } else if (key.isReadable()) {
                                SocketChannel sc = (SocketChannel) key.channel();
                                TcpChannel channel = channelMap.get(sc.getRemoteAddress());
                                channel.read();
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
            } catch (IOException e) {
                onError(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onOpen(BaseSocket socket) throws BeanException, IOException {
        socket.set(Constant.TYPE, Constant.SERVER);
        System.out.println("新连接接入: " + socket.getRemoteAddress().toString());
    }

    @Override
    public void close() {
        try {
            if (server.isOpen()) {
                server.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
