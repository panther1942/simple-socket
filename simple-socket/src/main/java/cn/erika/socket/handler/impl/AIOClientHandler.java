package cn.erika.socket.handler.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

public class AIOClientHandler implements Runnable {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private AsynchronousSocketChannel client;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    public AIOClientHandler() {
        try {
            this.client = AsynchronousSocketChannel.open();
            this.client.connect(new InetSocketAddress(ADDRESS, PORT)).get();
            this.buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public AIOClientHandler(String address, int port) {
        try {
            this.client = AsynchronousSocketChannel.open();
            this.client.connect(new InetSocketAddress(address, port)).get();
            this.buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AIOClientHandler client = new AIOClientHandler();
        new Thread(client).start();
    }

    @Override
    public void run() {
        while (client.isOpen()) {
            try {
                this.client.read(buffer).get();
                buffer.flip();
                String message = CHARSET.decode(buffer).toString();
                System.out.println("From Server: " + message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
