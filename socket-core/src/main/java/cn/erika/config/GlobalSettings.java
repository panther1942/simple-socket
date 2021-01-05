package cn.erika.config;

import cn.erika.utils.io.compress.stream.GZIP;
import cn.erika.utils.log.LogLevel;
import cn.erika.utils.security.DigitalSignatureAlgorithm;
import cn.erika.utils.security.SecurityAlgorithm;
import cn.erika.utils.security.algorithm.BasicDigitalSignatureAlgorithm;
import cn.erika.utils.security.algorithm.BasicSecurityAlgorithm;

import java.nio.charset.Charset;

public class GlobalSettings {
    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT = 43037;

    public static Charset charset = Charset.forName(Constant.UTF8);
    public static String type = Constant.BIO;

    public static boolean logEnable = true;
    public static String logDir = "log/";
    public static String logName = "simple-socket";
    public static LogLevel logLevel = LogLevel.DEBUG;

    public static byte[] privateKey;
    public static byte[] publicKey;

    public static String dbDriver = "com.mysql.jdbc.Driver";
//    public static String dbUrl = "jdbc:mysql://127.0.0.1:3306/db_development";
    public static String dbUrl = "jdbc:sqlite:/home/erika/Workspaces/simple-socket/localStorage.db";
    public static String dbUsername = "test";
    public static String dbPassword = "test";
    public static String dbTestSql = "SELECT 0";

    // 对称加密算法
    public static SecurityAlgorithm securityAlgorithm = BasicSecurityAlgorithm.AES192CBC;
    // 对称加密长度
    public static int securityLength = 18;
    // 签名算法
    public static DigitalSignatureAlgorithm signAlgorithm = BasicDigitalSignatureAlgorithm.SHA256withRSA;

    public static boolean enableCompress = true;
    public static int compressCode = GZIP.CODE;

    public static String prompt = "> ";

}
