package cn.erika.config;

import cn.erika.socket.core.DataInfo;
import cn.erika.util.security.MessageDigest;
import cn.erika.util.security.Security;

import java.nio.charset.Charset;

public class GlobalSettings {
    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT = 12345;
    public static Charset charset = Charset.forName("UTF-8");

    public static String type = Constant.NIO;

    public static String username = "admin";
    public static String password = "admin";

    public static int poolSize = 5;
    public static int passwordLength = 18;
    public static Security.Type passwordType = Security.Type.AES256ECB;
    public static int rsaLength = 2048;
    public static String rsaAlgorithm = "SHA384WITHRSA";
    public static MessageDigest.Type fileSignAlgorithm = MessageDigest.Type.SHA384;

    // 启用压缩
    public static boolean enableCompress = false;
    // 压缩格式
    public static DataInfo.Compress compressMethod = DataInfo.Compress.GZIP;

    public static byte[] privateKey;
    public static byte[] publicKey;

    public static String baseDir = "download/";

}
