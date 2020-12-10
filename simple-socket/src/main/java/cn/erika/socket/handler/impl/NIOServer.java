package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.core.TcpChannel;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NIOServer extends AbstractServerHandler implements IServer, Runnable {
    private ServerSocketChannel server;
    private Selector selector = Selector.open();
    private Map<SelectionKey, TcpChannel> channelMap = new HashMap<>();

    public NIOServer(String address, int port) throws IOException {
        this.linkManager = new LinkManager();
        this.server = ServerSocketChannel.open();
        this.server.configureBlocking(false);
        this.server.bind(new InetSocketAddress(address, port));
        this.server.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        while (this.server.isOpen()) {
            try {
                int events = selector.select();
                System.out.println(events);
                if (events > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        if (key.isAcceptable()) {
                            TcpChannel channel = new TcpChannel(server.accept(), this, CHARSET);
                            channel.register(selector, SelectionKey.OP_READ);
                            channelMap.put(key, channel);
                        } else if (key.isReadable()) {
                            TcpChannel channel = channelMap.get(key);
                            channel.read();
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
