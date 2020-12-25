package cn.erika.util.security;

public enum MessageDigestAlgorithm {
    MD5("MD5"),
    SHA1("SHA-1"),
    SHA224("SHA-224"),
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512");

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
