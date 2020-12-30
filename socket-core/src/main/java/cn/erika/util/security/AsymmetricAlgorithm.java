package cn.erika.util.security;

/**
 * 不对称加密算法的枚举类
 * 目前只有RSA能用于加密 DSA和EC都是签名算法
 * 先扔这 万一啥时候用到了 也好扩展
 */
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
