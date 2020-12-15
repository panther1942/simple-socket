package cn.erika.test;

import cn.erika.aop.annotation.PackageScan;
import cn.erika.aop.component.Application;
import cn.erika.aop.exception.BeanException;
import cn.erika.socket.component.Message;
import cn.erika.util.compress.CompressException;
import cn.erika.util.compress.GZIP;
import cn.erika.util.string.SerialUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

public class CoreTest {

    @Test
    public void testInfo() throws CompressException {
        String msg = "hello world";
        byte[] encode = GZIP.compress(msg);

        byte[] decode = GZIP.uncompress(encode);
        System.out.println(new String(decode));
    }

    @Test
    public void testSerial() throws IOException {
        Message message = new Message("Hello");
        message.add("message", "World");
        byte[] data = SerialUtils.serialObject(message);
        Message msg = SerialUtils.serialObject(data);
        System.out.println(msg.toString());
    }

    @PackageScan("cn.erika")
    public static class TestApp extends Application {

        /*static {
            PackageScanner scanner = PackageScanner.getInstance();
            scanner.addHandler(new PackageScannerHandler() {
                @Override
                public boolean filter(Class<?> clazz) {
                    return clazz.getAnnotation(Component.class) != null;
                }

                @Override
                public void deal(Class<?> clazz) {
                    Component component = clazz.getAnnotation(Component.class);
                }
            });
        }*/
        IDemoService demoService;

        @Override
        public void afterStartup() {
            try {
                demoService = getBean("demo");
                new Thread(new Handler()).start();
                new Thread(new Handler()).start();
            } catch (BeanException e) {
                e.printStackTrace();
            }
        }

        private class Handler implements Runnable {
            @Override
            public void run() {
                try {
                    demoService.test(UUID.randomUUID().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        public static void main(String[] args) {
            TestApp.run(TestApp.class);
        }
    }
}
