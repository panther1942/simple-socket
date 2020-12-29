package cn.erika;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpTest {

    @Test
    public void testDatagramChannel() {
        InetSocketAddress address = new InetSocketAddress("localhost", 12345);

        Thread receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    datagramReceive(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    datagramSend(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        receiver.start();
        sender.start();

        try {
            receiver.join();
            sender.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void datagramSend(InetSocketAddress address) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress("localhost", 1234));
        ByteBuffer byteBuffer = ByteBuffer.allocate(channel.socket().getSendBufferSize());
        byteBuffer.clear();
        byteBuffer.put("Hello Server".getBytes());
        byteBuffer.flip();
        channel.send(byteBuffer, address);

        byteBuffer.clear();
        channel.receive(byteBuffer);
        byteBuffer.flip();

        byte[] arr = byteBuffer.array();
        int len = byteBuffer.limit();
        byte[] data = new byte[len];
        System.arraycopy(arr, 0, data, 0, len);
        System.out.println(new String(data));
    }

    private void datagramReceive(InetSocketAddress address) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(address);
        System.out.println("监听端口: " + address);
        ByteBuffer byteBuffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
        byteBuffer.clear();
        channel.receive(byteBuffer);
        byteBuffer.flip();

        byte[] arr = byteBuffer.array();
        int len = byteBuffer.limit();
        byte[] data = new byte[len];
        System.arraycopy(arr, 0, data, 0, len);
        System.out.println(new String(data));

        try {
            SocketAddress remoteAddress = channel.socket().getRemoteSocketAddress();
            if (remoteAddress != null) {
                byteBuffer.clear();
                byteBuffer.put("Hello Client".getBytes());
                byteBuffer.flip();
                channel.send(byteBuffer, remoteAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
