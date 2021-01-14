package cn.erika.config;

import cn.erika.utils.io.compress.stream.GZIP;
import cn.erika.utils.log.LogLevel;
import cn.erika.utils.security.DigitalSignatureAlgorithm;
import cn.erika.utils.security.SecurityAlgorithm;
import cn.erika.utils.security.algorithm.BasicDigitalSignatureAlgorithm;
import cn.erika.utils.security.algorithm.BasicMessageDigestAlgorithm;
import cn.erika.utils.security.algorithm.BasicSecurityAlgorithm;

import java.nio.charset.Charset;

public class GlobalSettings {
    // 默认的服务器地址
    public static final String DEFAULT_ADDRESS = "localhost";
    // 默认的服务器端口
    public static final int DEFAULT_PORT = 43037;

    // 传输使用的字符编码
    public static Charset charset = Charset.forName(Constant.UTF8);
    // 运行模式 支持BIO和NIO
    public static String type = Constant.BIO;
    public static long cleanInterval = 5 * 30 * 1000;
    public static long invalidInterval = 5 * 60 * 1000;

    // 启动日志
    public static boolean logEnable = true;
    // 日志级别
    public static LogLevel logLevel = LogLevel.DEBUG;
    // 文件日志的存放目录
    public static String logDir = "log/";
    // 文件日志的前缀
    public static String logName = "simple-socket";

    // RSA Private Key
    public static byte[] privateKey;
    // RSA Public Key
    public static byte[] publicKey;

    // 测试中发现只要只要提供了驱动 就算不写驱动名也没问题
    public static String dbDriver = "com.mysql.jdbc.Driver";
    public static String dbUrl = "jdbc:mysql://127.0.0.1:3306/db_development";
    //    public static String dbUrl = "jdbc:sqlite:/home/erika/Workspaces/simple-socket/localStorage.db";
    public static String dbUsername = "test"; // sqlite不需要
    public static String dbPassword = "test"; // sqlite不需要
    // 用于测试连通性的语句
    public static String dbTestSql = "SELECT 0";

    // 对称加密算法
    public static SecurityAlgorithm securityAlgorithm = BasicSecurityAlgorithm.AES192CBC;
    // 对称加密长度
    public static int securityLength = 18;
    // 签名算法
    public static DigitalSignatureAlgorithm signAlgorithm = BasicDigitalSignatureAlgorithm.SHA256withRSA;
    // 是否启用数据压缩
    public static boolean enableCompress = true;
    // 压缩方式 目前只有GZIP能用 如果有其他算法请自行扩展
    public static int compressCode = GZIP.CODE;

    // 貌似并没啥用 因为输出会让提示符跑到前面去
    public static String prompt = "> ";

    // 客户端传输文件使用的线程数量
    public static int threads = 4;
    // 服务器传输文件对线程的限制数量（MAX）
    public static int threadsLimit = 10;
    public static String fileSignAlgorithm = BasicMessageDigestAlgorithm.SHA1.getValue();
    public static int fileTransBlock = 4 * 1024 * 1024;

}
