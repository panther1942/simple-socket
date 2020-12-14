package cn.erika.test;

import cn.erika.util.string.KeyboardReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NIOClient implements Runnable {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static KeyboardReader reader = KeyboardReader.getInstance();

    private String address;
    private int port;

    private SocketChannel client;
    private Selector selector;

    public NIOClient() {
        try {
            this.address = ADDRESS;
            this.port = PORT;
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NIOClient(String address, int port) {
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
        this.client = SocketChannel.open();
        this.client.configureBlocking(false);
        this.client.register(this.selector, SelectionKey.OP_CONNECT);
        this.client.connect(new InetSocketAddress(address, port));
    }

    public static void main(String[] args) {
        NIOClient client = new NIOClient();
        new Thread(client).start();
        String line;
        while ((line = reader.read()) != null) {
            client.write(line);
        }
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(this.selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap("Hello Server".getBytes(CHARSET)));
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        if (channel.read(buffer) > 0) {
            buffer.flip();
            System.out.println("Receive From Server: " + new String(buffer.array()));
        }
    }

    @Override
    public void run() {
        while (client.isOpen()) {
            try {
                int events = selector.select();
//                System.out.println(events);
                if (events > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        if (key.isConnectable()) {
                            connect(key);
                        } else if (key.isReadable()) {
                            read(key);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String message) {
        try {
//            client.register(selector, SelectionKey.OP_WRITE);
//            System.out.println("Write status: " + ((client.validOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE));
            client.write(ByteBuffer.wrap(message.getBytes()));
            selector.wakeup();
//            client.register(selector, SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
