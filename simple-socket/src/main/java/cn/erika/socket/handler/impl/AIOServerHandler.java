package cn.erika.socket.handler.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AIOServerHandler implements Runnable {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private AsynchronousServerSocketChannel server;

    public AIOServerHandler() {
        try {
            this.server = AsynchronousServerSocketChannel.open();
            this.server.bind(new InetSocketAddress(ADDRESS, PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AIOServerHandler(String address, int port) {
        try {
            this.server = AsynchronousServerSocketChannel.open();
            this.server.bind(new InetSocketAddress(address, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AIOServerHandler server = new AIOServerHandler();
        new Thread(server).start();
    }

    @Override
    public void run() {
        while (this.server.isOpen()) {
            try {
                Future<AsynchronousSocketChannel> future = server.accept();
                AsynchronousSocketChannel client = future.get();

//                System.out.println(client.getRemoteAddress().toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class Handler{
        private AsynchronousSocketChannel client;

        public Handler(AsynchronousSocketChannel client) {
            this.client = client;
        }

        public void send(String message) {
            try {
                client.write(ByteBuffer.wrap("Hello World!".getBytes(CHARSET))).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
