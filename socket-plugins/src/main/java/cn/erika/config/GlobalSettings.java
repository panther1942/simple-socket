package cn.erika.config;

import cn.erika.socket.core.component.DataInfo;
import cn.erika.util.log.LogLevel;
import cn.erika.util.security.AsymmetricAlgorithm;
import cn.erika.util.security.DigitalSignatureAlgorithm;
import cn.erika.util.security.SecurityAlgorithm;

import java.nio.charset.Charset;

public class GlobalSettings {
    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT = 43037;
    public static Charset charset = Charset.forName(Constant.UTF8);
    public static String type = Constant.BIO;
    public static boolean devMode = true;

    public static boolean logEnable = true;
    public static String logDir = "log/";
    public static String logName = "simple-socket";
    public static LogLevel logLevel = LogLevel.DEBUG;

    public static byte[] privateKey;
    public static byte[] publicKey;

    // 对称加密算法
    public static SecurityAlgorithm securityAlgorithm = SecurityAlgorithm.AES256ECB;
    // 对称加密长度
    public static int securityLength = 18;
    // 不对称加密算法 现在的不对称加密好像就RSA 其他的都是签名算法
    public static AsymmetricAlgorithm asymmetricAlgorithm = AsymmetricAlgorithm.RSA;
    // 不对称加密长度
    public static int asymmetricKeyLength = 2048;
    // 签名算法
    public static DigitalSignatureAlgorithm digitalSignatureAlgorithm = DigitalSignatureAlgorithm.SHA256withRSA;

    public static boolean enableCompress = true;
    public static DataInfo.Compress compressType = DataInfo.Compress.GZIP;

    public static String baseDir = "download/";

}
