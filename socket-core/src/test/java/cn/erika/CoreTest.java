package cn.erika;

import cn.erika.service.DemoServiceImpl;
import cn.erika.service.IDemoService;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.MessageDigest;
import cn.erika.util.string.Base64Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class CoreTest {

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
    public void testCrc() throws IOException {
//        System.out.println(MessageDigest.crc32Sum("admin".getBytes()));
        File file = new File("/home/erika/Downloads/phpMyAdmin-5.0.4-all-languages.zip");
//        File file = new File("/home/erika/IdeaProjects/simple-socket/downloads/phpMyAdmin.zip");
        long checkCode = MessageDigest.crc32Sum(file);
        System.out.println(checkCode);
    }

    @Test
    public void testBase64() {
        /*System.out.println(Character.hashCode('A'));
        System.out.println('A' >> 2);
        System.out.println('A' & 0x00000011);

        System.out.println(Character.toChars(Base64Utils.base64Map[('A' & 0x00000011) + 1]));*/

//        String src = "Hello World";
        String src = "你好 世界";
        byte[] encrypt = Base64Utils.encode(src.getBytes());
        System.out.println(new String(encrypt));
        byte[] decrypt = Base64Utils.decode(encrypt);
        System.out.println(new String(decrypt));
    }

    @Test
    public void testHex() {
        String sign = "b15328fe3971ffac07b4ea92a9119f5c";
        for (int i = 0; i < sign.length(); i++) {
            char c = sign.charAt(i);
            System.out.println(Integer.parseInt(String.valueOf(c), 16));
        }
    }
}
