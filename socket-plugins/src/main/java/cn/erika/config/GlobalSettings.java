package cn.erika.config;

import cn.erika.socket.core.component.DataInfo;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.*;

import java.nio.charset.Charset;

public class GlobalSettings {
    public static final String DEFUALT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT = 43037;
    public static Charset charset = Charset.forName(Constant.UTF8);
    public static String type = Constant.BIO;
    public static int logLevel = LoggerFactory.DEBUG;

    public static byte[] privateKey;
    public static byte[] publicKey;
    public static int passwordLength = 18;
    public static SecurityAlgorithm passwordAlgorithm = SecurityAlgorithm.AES256ECB;
    public static int rsaLength = 2048;
    public static RSADigestAlgorithm rsaDigestAlgorithm = RSADigestAlgorithm.SHA384WITHRSA;
    public static MessageDigestAlgorithm fileSignAlgorithm = MessageDigestAlgorithm.SHA384;

    public static boolean enableCompress = true;
    public static DataInfo.Compress compressType = DataInfo.Compress.GZIP;

    public static String baseDir = "download/";

}
