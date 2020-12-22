package cn.erika.util.security;

public enum RSADigestAlgorithm {
    SHA1WITHRSA("SHA1WITHRSA"),
    SHA224WITHRSA("SHA224WITHRSA"),
    SHA256WITHRSA("SHA256WITHRSA"),
    SHA384WITHRSA("SHA384WITHRSA"),
    SHA512WITHRSA("SHA512WITHRSA");

    private String value;

    RSADigestAlgorithm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
