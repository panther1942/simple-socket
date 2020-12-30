package cn.erika.util.security;

/**
 * 消息签名算法的枚举类
 * 建议非重要信息使用MD5或者CRC32
 * CRC32不是消息签名 只是确保消息完整
 */
public enum MessageDigestAlgorithm {
    MD5("MD5"),
    SHA1("SHA1"),
    SHA224("SHA224"),
    SHA256("SHA256"),
    SHA384("SHA384"),
    SHA512("SHA512");

    private String value;

    MessageDigestAlgorithm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
