package cn.erika.util.security.algorithm;

import cn.erika.util.security.AsymmetricAlgorithm;

/**
 * 不对称加密算法的枚举类
 * 目前只有RSA能用于加密 DSA是签名算法
 * EC则没有完整的支持 没法用 第三方库还没测试 暂不提供
 */
public enum BasicAsymmetricAlgorithm implements AsymmetricAlgorithm {
    RSA("RSA", "SunJSSE"),
    DSA("DSA", "SunJSSE");

    private String value;
    private String provider;

    BasicAsymmetricAlgorithm(String value, String provider) {
        this.value = value;
        this.provider = provider;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getProvider() {
        return provider;
    }
}
