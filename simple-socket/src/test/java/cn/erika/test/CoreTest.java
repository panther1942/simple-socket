package cn.erika.test;

import cn.erika.aop.annotation.PackageScan;
import cn.erika.aop.component.Application;
import cn.erika.aop.exception.BeanException;
import cn.erika.socket.component.Message;
import cn.erika.util.compress.CompressException;
import cn.erika.util.compress.GZIP;
import cn.erika.util.string.SerialUtils;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.Test;

import java.io.IOException;
import java.security.Provider;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Test
    public void testSecurity() {
        for (Provider provider : java.security.Security.getProviders()) {
            System.out.println(provider.getName());
        }
    }

    @Test
    public void testFormatPrint() {
        String name = "admin";
        int age = 10;
        System.out.printf("%18s\t%010d%3c", name, age, 'a');
    }

    public static class CliApplication {
        public static void main(String[] args) {
            try {
                Completer commandCompleter = new StringsCompleter("CREATE", "OPEN", "WRITE", "CLOSE");
                Terminal terminal = TerminalBuilder.builder().system(true).build();
                LineReader reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .completer(commandCompleter)
                        .build();
                String prompt = "> ";
                String line;
                while (!"exit".equalsIgnoreCase(line = reader.readLine(prompt))) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PackageScan("cn.erika")
    public static class TestApp extends Application {
        private ExecutorService service = Executors.newCachedThreadPool();
        public static TestApp app;

        public static void main(String[] args) {
//            TestApp.run(TestApp.class);
            try {
                app = new TestApp();
//                app.run();
                app = null;
                System.gc();
                Thread.sleep(500);
                if (app == null) {
                    System.out.println("app is destroy1");
                } else {
                    System.out.println("app is alive1");
                }
                app = null;
                System.gc();
                Thread.sleep(500);
                if (app == null) {
                    System.out.println("app is destroy2");
                } else {
                    System.out.println("app is alive2");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterStartup() {
            try {
                service.submit(new Handler(getBean("demo")));
                service.submit(new Handler(getBean("demo")));
                service.shutdown();
            } catch (BeanException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            System.out.println("END");
            app = this;
        }

        private class Handler implements Runnable {
            IDemoService demoService;

            public Handler(IDemoService demoService) {
                this.demoService = demoService;
            }

            @Override
            public void run() {
                try {
                    demoService.test(UUID.randomUUID().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
