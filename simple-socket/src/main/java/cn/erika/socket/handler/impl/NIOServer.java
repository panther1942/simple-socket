package cn.erika.socket.handler.impl;

import cn.erika.socket.nio.core.AbstractHandler;
import cn.erika.socket.nio.core.TcpChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class NIOServer extends AbstractHandler {
    private ServerSocketChannel server;
    private Map<SocketChannel, TcpChannel> channelMap = new HashMap<>();

    public NIOServer(String address, int port) throws IOException {
        super();
        this.server = ServerSocketChannel.open();
        this.server.configureBlocking(false);
        this.server.bind(new InetSocketAddress(address, port));
        register(this.server, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void onAccept(SelectionKey selectionKey) {
        try {
            SocketChannel channel = server.accept();
            channelMap.put(channel, new TcpChannel(channel, this, charset));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(SelectionKey selectionKey) {

    }

    @Override
    public void onEstablished(TcpChannel channel) {

    }

    @Override
    public void onMessage(SelectionKey selectionKey) {
        TcpChannel channel = channelMap.get((SocketChannel) selectionKey.channel());
        channel.read();

    }
}
