package cn.erika;

import cn.erika.enumTest.Food;
import cn.erika.enumTest.Fruit;
import cn.erika.enumTest.Vegetables;
import cn.erika.service.DemoServiceImpl;
import cn.erika.service.IDemoService;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.UnsupportedAlgorithmException;
import cn.erika.util.SerialUtils;
import cn.erika.util.log.ConsoleLogger;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.algorithm.AsymmetricAlgorithm;
import cn.erika.util.security.MessageDigestUtils;
import cn.erika.util.security.SecurityUtils;
import cn.erika.util.string.Base64Utils;
import cn.erika.util.string.ConsoleUtils;
import cn.erika.util.string.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
        Thread thread = Thread.currentThread();
        thread.setName("123456789ABCDEFGHIJKL");
        LoggerFactory.register(new ConsoleLogger());
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.debug("This is debug information");
        log.info("This is info information");
        log.warn("This is warn information");
        log.error("This is error information");
    }

    @Test
    public void testCrc() throws IOException {
//        System.out.println(MessageDigestUtils.crc32Sum("admin".getBytes()));
        File file = new File("/home/erika/Downloads/phpMyAdmin-5.0.4-all-languages.zip");
//        File file = new File("/home/erika/IdeaProjects/simple-socket/downloads/phpMyAdmin.zip");
        long checkCode = MessageDigestUtils.crc32Sum(file);
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

    @Test
    public void testSerial() {
        File srcFile = new File("/home/erika/Downloads/phpMyAdmin-5.0.4-all-languages.zip");
        File destFile = new File("downloads/phpMyAdmin.zip");
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            destFile.createNewFile();
        } catch (IOException e) {
            System.err.println(destFile.getAbsolutePath());
            e.printStackTrace();
        }
        try (RandomAccessFile fileReader = new RandomAccessFile(srcFile, "r");
             RandomAccessFile fileWriter = new RandomAccessFile(destFile, "rw")) {
            System.out.println(MessageDigestUtils.crc32Sum(srcFile));
            int len = 0;
            byte[] tmp = new byte[4096];
            while ((len = fileReader.read(tmp)) > -1) {
                Map<String, Object> srcPak = new HashMap<>();
                srcPak.put("LEN", len);
                srcPak.put("BIN", Base64Utils.encode(tmp));

                byte[] data = SerialUtils.serialObject(srcPak);
                Map<String, Object> destPak = SerialUtils.serialObject(data);
                int len2 = (int) destPak.get("LEN");
                byte[] tmp2 = Base64Utils.decode((byte[]) destPak.get("BIN"));

                fileWriter.write(tmp2, 0, len2);
            }
            System.out.println(MessageDigestUtils.crc32Sum(destFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSupportSecurity() {
        String target = "EC";
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            System.out.println(ConsoleUtils.drawLineWithTitle(provider.getName(), "-", 80));
            for (Provider.Service service : provider.getServices()) {
                String algorithm = service.getAlgorithm();
                if (algorithm.contains(target)) {
                    System.err.println(algorithm);
                } else {
                    System.out.println(algorithm);
                }
            }
        }
    }

    @Test
    public void printProvider() {
        Provider provider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        for (Provider.Service service : provider.getServices()) {
            System.out.println(String.format("%s: %s", service.getType(), service.getAlgorithm()));
        }
    }

    @Test
    public void testEc() {
        try {
            byte[][] keyPair = SecurityUtils.initEcKey();
            String data = StringUtils.randomString(1024);

            byte[] enData = SecurityUtils.encrypt(data.getBytes(), keyPair[0], AsymmetricAlgorithm.EC);

            byte[] deData = SecurityUtils.decrypt(enData, keyPair[1], AsymmetricAlgorithm.EC);

            System.out.println(new String(deData));


        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRandomString() {
        for (int i = 0; i < 50; i++) {
//            System.out.println(StringUtils.randomString(32));
            System.out.println(StringUtils.randomNumber(32));
        }
    }

    @Test
    public void testGetParam() {
        String test = "Hello World 'This is my program' test \"what's your name\" `hold your fire`";
        String[] params = StringUtils.getParam(test);
        for (String param : params) {
            System.out.println(param);
        }
    }

    @Test
    public void testByte2HexString() {
        System.out.println(String.format("%02X", 0xA1));

        byte[] data = new byte[]{
                'a', 'c', '1', '-'
        };

        String hexString = StringUtils.byte2HexString(data);

        System.out.println(hexString);

        byte[] target = StringUtils.hexString2Byte(hexString);
        System.out.println(new String(target));
    }

    @Test
    public void testRsa() {
        try {
            int rsa1024 = 117;
            int rsa2048 = 245;

            String line = StringUtils.randomString(rsa1024);
            byte[][] keyPair = SecurityUtils.initKey(AsymmetricAlgorithm.RSA, 1024);

            System.out.println(Base64.getEncoder().encodeToString(keyPair[0]));
            System.out.println();

            byte[] enData = SecurityUtils.encrypt(line.getBytes(), keyPair[0]);
            byte[] deData = SecurityUtils.decrypt(enData, keyPair[1]);

            System.out.println(new String(deData));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMessage() {
        Message message = new Message();
        Boolean isEnable = message.get("enabled");
        System.out.println(isEnable);
    }

    @Test
    public void testEnum() {
        Fruit a = Fruit.苹果;
        Vegetables b = Vegetables.大白菜;

        eatFood(a);
        eatFood(b);
    }

    private static void eatFood(Food food) {
        food.eat();
        // 接口不能用switch
        /*switch (food) {
            case Fruit.桃子:
                break;
        }*/
    }
}
