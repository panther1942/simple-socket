package cn.erika.test;

import cn.erika.config.GlobalSettings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOServer implements Runnable {

    private static final String ADDRESS = GlobalSettings.DEFAULT_ADDRESS;
    private static final int PORT = GlobalSettings.DEFAULT_PORT;
    private static final Charset CHARSET = GlobalSettings.charset;

    private String address;
    private int port;

    private ServerSocketChannel server;
    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(GlobalSettings.poolSize);
    private Vector<SocketChannel> channels = new Vector<>();


    public NIOServer() {
        try {
            this.address = ADDRESS;
            this.port = PORT;
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NIOServer(String address, int port) {
        try {
            this.address = address;
            this.port = port;
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        this.selector = Selector.open();
        this.server = ServerSocketChannel.open();
        this.server.configureBlocking(false);
        this.server.bind(new InetSocketAddress(address, port));
        this.server.register(this.selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务启动: " + this.address + ":" + this.port);
    }

    public static void main(String[] args) {
        NIOServer server = new NIOServer();
        new Thread(server).start();
    }

    private void accept(SocketChannel channel) {
        try {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            System.out.println("新连接接入: " + channel.getRemoteAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (this.server.isOpen()) {
            try {
                int events = selector.select();
//                System.out.println(events);
                if (events > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
//                        System.out.println("Key status: " + key.readyOps());
                        if (key.isAcceptable()) {
                            SocketChannel channel = server.accept();
                            accept(channel);
                            channels.add(channel);
                        } else {
                            SocketChannel channel = (SocketChannel) key.channel();
                            System.out.println(channels.contains(channel));
                            service.submit(new Handler(key));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Handler implements Runnable {
        private SelectionKey key;
        private SocketChannel channel;

        public Handler(SelectionKey key) {
            this.key = key;
            this.channel = (SocketChannel) key.channel();
        }

        @Override
        public void run() {
            try {
                try {
                    if (key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        if (channel.read(buffer) > 0) {
                            buffer.flip();
                            System.out.println("Receive From: " + channel.getRemoteAddress().toString() +
                                    "[" + new String(buffer.array()) + "]");
//                            channel.register(selector, SelectionKey.OP_WRITE);
                            channel.write(ByteBuffer.wrap("Hello World".getBytes(CHARSET)));
                            selector.wakeup();
//                            channel.register(selector, SelectionKey.OP_READ);
                            buffer.clear();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("客户端断开连接");
                    key.cancel();
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
