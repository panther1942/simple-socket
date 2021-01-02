package cn.erika.socket.handler.nio;

import cn.erika.socket.core.tcp.TcpChannel;
import cn.erika.socket.handler.BasicClient;
import cn.erika.socket.handler.IClient;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class NIOClient extends BasicClient implements Runnable, IClient {
    private SocketAddress address;
    private Selector selector;

    public NIOClient(SocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() throws IOException {
        this.selector = Selector.open();
        this.socket = new TcpChannel(address, this, selector);
        Thread thread = new Thread(this, this.getClass().getSimpleName());
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                int events = selector.select();
                if (events > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        if (key.isConnectable()) {
                            finishConnect(key);
                        } else if (key.isReadable()) {
                            try {
                                TcpChannel channel = (TcpChannel) socket;
                                channel.read();
                            } catch (ClosedChannelException | CancelledKeyException e) {
                                log.warn("连接被重置");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                onError(socket, e);
                close();
                break;
            }
        }
    }

    private void finishConnect(SelectionKey key) throws IOException {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            channel.register(this.selector, SelectionKey.OP_READ);
        } catch (ConnectException e) {
            // 说明服务器不在线
            log.warn(e.getMessage());
        }
    }
}
