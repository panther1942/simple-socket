package cn.erika.util.security;

/**
 * 数字签名算法的枚举类 配合不对称加密算法使用
 */
public enum DigitalSignatureAlgorithm {
    MD2withRSA("MD2withRSA"),
    MD5withRSA("MD5withRSA"),
    SHA1withRSA("SHA1withRSA"),
    SHA224withRSA("SHA224withRSA"),
    SHA256withRSA("SHA256withRSA"),
    SHA384withRSA("SHA384withRSA"),
    SHA512withRSA("SHA512withRSA"),

    NONEwithDSA("NONEwithDSA"),
    SHA1withDSA("SHA1withDSA"),
    SHA224withDSA("SHA224withDSA"),
    SHA256withDSA("SHA256withDSA"),

    NONEwithECDSA("NONEwithECDSA"),
    SHA1withECDSA("SHA1withECDSA"),
    SHA224withECDSA("SHA224withECDSA"),
    SHA256withECDSA("SHA256withECDSA"),
    SHA384withECDSA("SHA384withECDSA"),
    SHA512withECDSA("SHA512withECDSA");

    private String value;

    DigitalSignatureAlgorithm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
