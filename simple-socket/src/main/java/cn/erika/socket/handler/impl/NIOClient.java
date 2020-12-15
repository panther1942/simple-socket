package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.socket.core.TcpChannel;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOClient extends AbstractClientHandler implements Runnable {
    private Selector selector;
    private Thread thread;

    public NIOClient(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() {
        try {
            this.selector = Selector.open();
            this.socket = new TcpChannel(address, this, selector, CHARSET);
            this.thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            onError(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        while (!this.socket.isClosed()) {
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
                            TcpChannel channel = (TcpChannel) this.socket;
                            channel.read();
                        }
                    }
                }
            } catch (IOException e) {
                onError(e.getMessage(), e);
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
            onOpen(this.socket);
        } catch (ConnectException e) {
            log.warn(e.getMessage());
            onClose(this.socket);
        } catch (BeanException e) {
            onError(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        super.close();
        System.out.println(thread.isInterrupted());
    }
}
