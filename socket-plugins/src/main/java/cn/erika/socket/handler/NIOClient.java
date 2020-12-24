package cn.erika.socket.handler;

import cn.erika.socket.core.tcp.TcpChannel;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOClient extends Client implements Runnable {
    private SocketAddress address;
    private Selector selector;
    private Thread thread;

    public NIOClient(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() throws IOException {
        this.selector = Selector.open();
        this.socket = new TcpChannel(address, this, selector);
        this.thread = new Thread(this, this.getClass().getSimpleName());
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
                            } catch (IOException e) {
                                onError(socket, e);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                // 三个点
                // 1. selector出错 则应该停止运行 因为程序出错而不是网络出错
                // 2. finishConnect出错 则中断客户端运行 因为这块基本上都是服务没有开导致的
                // 3. channel.read出错 则发送离线消息后中断客户端运行 这块大概率是网络出错
                onError(socket, e);
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
            log.warn(e.getMessage());
            close();
            // 说明服务器不在线
        }
    }
}
