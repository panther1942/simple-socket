package cn.erika.util.security;

public enum AsymmetricAlgorithm {
    RSA("RSA"),
    DSA("DSA"),
    EC("EC");

    private String value;

    AsymmetricAlgorithm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
