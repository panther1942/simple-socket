package cn.erika;

import cn.erika.context.Application;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.service.DemoServiceImpl;
import cn.erika.service.IDemoService;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.MessageDigest;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@PackageScan("cn.erika")
public class TestApplication extends Application {
    private BeanFactory beanFactory = BeanFactory.getInstance();

    public static void main(String[] args) {
        run(TestApplication.class);
    }

    @Override
    protected void afterStartup() {
        try {
            System.out.println(beanFactory.execute("sum", 1, 2));
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInterface() {
        IDemoService demoService = new DemoServiceImpl();
        demoService.say();
        System.out.println(demoService.sum(1, 1));
        System.out.println("end");
    }

    @Test
    public void testLog() {
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.debug("This is debug information");
        log.info("This is info information");
        log.warn("This is warn information");
        log.error("This is error information");
    }

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

    @Test
    public void testCrc(){
        System.out.println(MessageDigest.crc32Sum("admin".getBytes()));
    }
}
