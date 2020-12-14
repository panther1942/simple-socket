package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.socket.core.TcpChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOClient extends AbstractClientHandler implements Runnable {
    private Selector selector;

    public NIOClient(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() {
        try {
            this.selector = Selector.open();
            this.socket = new TcpChannel(address, this, selector, CHARSET);
            new Thread(this).start();
        } catch (IOException e) {
            onError(e.getMessage(), e);
        }
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
                        if (key.isConnectable()) {
                            finishConnect(key);
                        } else if (key.isReadable()) {
//                            SocketChannel channel = (SocketChannel) key.channel();
                            TcpChannel channel = (TcpChannel) this.socket;
                            channel.read();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BeanException e) {
                e.printStackTrace();
            }
        }
    }

    private void finishConnect(SelectionKey key) throws IOException, BeanException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.register(this.selector, SelectionKey.OP_READ);
        onOpen(this.socket);
    }
}
