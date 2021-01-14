package cn.erika;

import cn.erika.config.Constant;
import cn.erika.enumTest.Food;
import cn.erika.enumTest.Fruit;
import cn.erika.enumTest.Vegetables;
import cn.erika.socket.model.po.Account;
import cn.erika.service.DemoServiceImpl;
import cn.erika.service.IDemoService;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.socket.orm.IAccountService;
import cn.erika.socket.orm.impl.AccountServiceImpl;
import cn.erika.utils.BinaryNode;
import cn.erika.utils.exception.UnsupportedAlgorithmException;
import cn.erika.utils.io.compress.file.ZIP;
import cn.erika.utils.exception.CompressException;
import cn.erika.utils.io.FileUtils;
import cn.erika.utils.log.ConsoleLogger;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.security.MessageDigestUtils;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.security.algorithm.BasicAsymmetricAlgorithm;
import cn.erika.utils.string.Base64Utils;
import cn.erika.utils.io.SerialUtils;
import cn.erika.utils.string.StringUtils;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.*;

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
        File file = new File("/var/lib/libvirt/images/cn_windows_7_ultimate_with_sp1_x64_dvd_u_677408.iso");
//        File file = new File("/home/erika/Downloads/phpMyAdmin-5.0.4-all-languages.zip");
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
        Date start = new Date();
        for (int i = 0; i < 100000; i++) {
            String src = StringUtils.randomString(1024);
//            byte[] encrypt = Base64Utils.encode(src.getBytes());
            byte[] encrypt = Base64.getEncoder().encode(src.getBytes());
//            System.out.println(new String(encrypt));
//            byte[] decrypt = Base64Utils.decode(encrypt);
            byte[] decrypt = Base64.getDecoder().decode(encrypt);
//            System.out.println(new String(decrypt));
        }
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime());
        // 9993ms cn.erika.utils.string.Base64Utils
        // 4253ms java.util.Base64
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

                byte[] data = SerialUtils.serialJavaObject(srcPak);
                Map<String, Object> destPak = SerialUtils.serialJavaObject(data);
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
        String target = "GCM";
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            for (Provider.Service service : provider.getServices()) {
                String type = service.getType();
                String algorithm = service.getAlgorithm();
                if (algorithm.contains(target)) {
                    System.err.printf("%-15s[%s]: %s\n", provider.getName(), type, algorithm);
                } else {
                    System.out.printf("%-15s[%s]: %s\n", provider.getName(), type, algorithm);
                }
            }
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
            byte[][] keyPair = SecurityUtils.initKey(BasicAsymmetricAlgorithm.RSA, 1024);

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

    @Test
    public void testZipCompress() throws IOException, CompressException {
        LoggerFactory.register(new ConsoleLogger());
        File in = new File("/home/erika/Downloads/config.json");
        File out = new File("downloads/config.zip");
        System.out.println(out.getAbsolutePath());
        ZIP zip = new ZIP();
        zip.compress(in, out);
    }

    @Test
    public void testZipDecompress() throws CompressException {
        LoggerFactory.register(new ConsoleLogger());
//        File in = new File("downloads/config.zip");
//        File out = new File("downloads");
        File in = new File("downloads/archive.zip");
        File out = new File("downloads/archive");
        System.out.println(out.getAbsolutePath());
        ZIP zip = new ZIP();
        zip.decompress(in, out);
    }

    @Test
    public void testZipCompressDir() throws IOException, CompressException {
        LoggerFactory.register(new ConsoleLogger());
        File in = new File("downloads");
        File out = new File("downloads/archive.zip");
        System.out.println(out.getAbsolutePath());
        ZIP zip = new ZIP();
        zip.compressDir(in, out);
    }

    @Test
    public void testZipFileRW() throws IOException, CompressException {
        String srcFilename = "srcFile.txt";
        String archiveName = "archive.zip";
        String unarchiveDir = "unarchive";

        String str = "Hello World";
        ZIP zip = new ZIP();

        FileUtils.writeFile(srcFilename, Base64Utils.encode(str.getBytes()));
        zip.compress(new File(srcFilename), new File(archiveName));
        zip.decompress(new File(archiveName), new File(unarchiveDir));
        byte[] arr = FileUtils.readFile(unarchiveDir + "/" + srcFilename);
        System.out.println(new String(Base64Utils.decode(arr)));
    }

    @Test
    public void testSelect() {
        String sql = "SELECT * FROM tb_account";
        List<Account> list = Account.dao.select(sql);
        for (Account acc : list) {
            System.out.println(acc);
        }
    }

    static class T1 {
        public static void main(String[] args) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    String sql = "SELECT * FROM tb_account";
                    List<Account> list = Account.dao.select(sql);
                    for (Account acc : list) {
                        System.out.println(acc);
                    }
                }
            };
            for (int i = 0; i < 15; i++) {
                new Thread(r).start();
            }
        }
    }

    @Test
    public void testInsert() {
        Account acc = new Account();
        acc.setUsername("admin");
        acc.setPassword("admin");
        acc.setCreateTime(new Date());
        acc.setUpdateTime(new Date());
        int count = acc.insert();
        System.out.println(count);
        testSelect();
    }

    @Test
    public void testUpdate() {
        LoggerFactory.register(new ConsoleLogger());
        String sql = "SELECT * FROM tb_account";
        List<Account> list = Account.dao.select(sql);
        if (list.size() > 0) {
            Account account = list.get(0);
            account.setPassword("admin");
            account.setUpdateTime(new Date());
            int count = account.update();
            System.out.println(count);
        }
        testSelect();
    }

    @Test
    public void testDelete() {
        String sql = "SELECT * FROM tb_account WHERE `username` = ?";
        List<Account> list = Account.dao.select(sql, "admin");
        if (list.size() > 0) {
            int count = list.get(0).delete();
            System.out.println(count);
        }
        testSelect();
    }

    @Test
    public void testService() {
        IAccountService accountService = new AccountServiceImpl();
        List<Account> list = accountService.getAll();
        for (Account acc : list) {
            System.out.println(acc);
        }
        if (list.size() > 0) {
            Account acc = list.get(0);
            acc.setPassword("admin");
            acc.update();
            System.out.println(acc);
        }
        list = accountService.getAll();
        for (Account acc : list) {
            System.out.println(acc);
        }
    }

    @Test
    public void testModel() {
        FileTransInfo info = new FileTransInfo();
        FileTransRecord recode = new FileTransRecord();
        recode.setFilename("123.txt");
        recode.setFilepath("~/123.txt");
        recode.setLength(10000000L);
        recode.setThreads(4);
        info.setRecord(recode);
        List<FileTransPartRecord> parts = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            FileTransPartRecord part = new FileTransPartRecord();
            part.setFilename(recode.getFilename() + Constant.FILE_PART_POSTFIX);
            part.setCreateTime(new Date());
            parts.add(part);
        }
        info.setParts(parts);
        System.out.println(info);
    }

    @Test
    public void testArraySerial() {
        Message message = new Message();
        String[] array = {
                "a", "b", "c"
        };
        message.add("arr", array);
        System.out.println(JSON.toJSONString(message));
    }

    @Test
    public void testMessageSerial() {
        String string = "{\"payload\":{\"filePos\":0,\"len\":268,\"bin\":\"b2NvbCI6ICJmcmVlZG9tIiwKICAgICJzZXR0aW5ncyI6IHt9CiAgfSx7CiAgICAicHJvdG9jb2wiOiAiYmxhY2tob2xlIiwKICAgICJzZXR0aW5ncyI6IHt9LAogICAgInRhZyI6ICJibG9ja2VkIgogIH1dLAogICJyb3V0aW5nIjogewogICAgInJ1bGVzIjogWwogICAgICB7CiAgICAgICAgInR5cGUiOiAiZmllbGQiLAogICAgICAgICJpcCI6IFsiZ2VvaXA6cHJpdmF0ZSJdLAogICAgICAgICJvdXRib3VuZFRhZyI6ICJibG9ja2VkIgogICAgICB9CiAgICBdCiAgfQp9Cg==\",\"sign\":\"ZvZ8/lCwvURVslwJ6NzXECV+kByHLJLPPUxLO+T/ykKFTEOjfNv1lSXTOrrq8IWqomtqrcsvkEtXwCTgg43O/sqgBVAKe1y0jnwGf7NrB+zkKYKb2q1qQLUOQ14pL/Kkc96H1kj/A4DREbpmV7+90Q0VkimIIrQnSssuynBuic9h8s3Mpcv9bkWHoNct9pu776gOvwVpYfKpM3YzIYpUlB7anqQ/gmy/IaDBsa6ZkQvQt8zHGULCQkQN+lJ678TJhPOvz9a5pSd4vPdsfeD1F5okcciGC1q432RkTpGJbNw8woz5EEvADBnDOuLOCiGUq/1U62UqilP+G2xDSmmB4Q==\",\"serverName\":\"srv_upload\"}}";
        Message message = JSON.parseObject(string, Message.class);
        System.out.println(message);
    }

    @Test
    public void testUUID() throws ClassNotFoundException {
        Class.forName("cn.erika.utils.security.algorithm.BasicMessageDigestAlgorithm");
        String uuid = UUID.randomUUID().toString();
        System.out.println(uuid + " " + uuid.length());
        System.out.println(String.valueOf(Integer.MAX_VALUE).length());
        System.out.println(String.valueOf(Long.MAX_VALUE).length());
        long crc = MessageDigestUtils.crc32Sum(uuid.getBytes());
        System.out.println(crc + " " + String.valueOf(crc).length());
    }

    @Test
    public void testTree(){
        BinaryNode<Integer> tree = new BinaryNode<>();
        tree.addNode(13);
        tree.addNode(25);
        tree.addNode(6);
        tree.addNode(44);
        tree.addNode(30);
        tree.addNode(27);
        tree.balance();
        tree = tree.getRoot();

        System.out.println(tree.getDepth());

        for (Integer num : tree.firstOrder()) {
            System.out.print(num+" ");
        }
        System.out.println();
        for (Integer num : tree.middleOrder()) {
            System.out.print(num+" ");
        }
        System.out.println();
        for (Integer num : tree.lastOrder()) {
            System.out.print(num+" ");
        }
        System.out.println();
    }

    @Test
    public void testTreeObject() {
        BinaryNode<Person> tree = new BinaryNode<>();
        tree.addNode(new Person("张三", 13));
        tree.addNode(new Person("李四", 25));
        tree.addNode(new Person("王五", 6));
        tree.addNode(new Person("赵六", 44));
        tree.addNode(new Person("孙七", 30));
        tree.addNode(new Person("周八", 27));

        tree.balance();
        tree = tree.getRoot();

        System.out.println(tree.getDepth());

        for (Person person : tree.firstOrder()) {
            System.out.println(person);
        }
        System.out.println();
        for (Person person : tree.middleOrder()) {
            System.out.println(person);
        }
        System.out.println();
        for (Person person : tree.lastOrder()) {
            System.out.println(person);
        }
        System.out.println();
    }

    private class Person implements Comparable<Person> {
        private String name;
        private int scope;

        public Person(String name, int scope) {
            this.name = name;
            this.scope = scope;
        }

        @Override
        public int compareTo(Person person) {
            if (person == null) {
                return 1;
            } else {
                return Integer.compare(this.scope, person.scope);
            }
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", scope=" + scope +
                    '}';
        }
    }
}
