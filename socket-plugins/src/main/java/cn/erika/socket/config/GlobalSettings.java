package cn.erika.socket.config;

import cn.erika.socket.core.component.DataInfo;

import java.nio.charset.Charset;

public class GlobalSettings {
    public static byte[] privateKey;
    public static byte[] publicKey;
    public static int rsaLength = 2048;

    public static Charset charset = Charset.forName("UTF-8");

    public static boolean enableCompress = true;
    public static DataInfo.Compress compressType = DataInfo.Compress.GZIP;
}
