package cn.erika.util.security;

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

    public static MessageDigestAlgorithm getByName(String name) {
        for (MessageDigestAlgorithm algorithm : MessageDigestAlgorithm.values()) {
            if (algorithm.value.equals(name)) {
                return algorithm;
            }
        }
        return null;
    }
}
