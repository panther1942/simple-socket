package cn.erika.utils.security.algorithm;

import cn.erika.utils.security.SecurityAlgorithm;
import cn.erika.utils.security.SecurityUtils;

/**
 * 对称加密算法的枚举类
 */
public enum BasicSecurityAlgorithm implements SecurityAlgorithm {
    AES128ECB("AES128ECB", "AES", "ECB", 128, false, 0),
    AES192ECB("AES192ECB", "AES", "ECB", 192, false, 0),
    AES256ECB("AES256ECB", "AES", "ECB", 256, false, 0),
    AES128CBC("AES128CBC", "AES", "CBC", 128, true, 16),
    AES192CBC("AES192CBC", "AES", "CBC", 192, true, 16),
    AES256CBC("AES256CBC", "AES", "CBC", 256, true, 16),
    AES128CTR("AES128CTR", "AES", "CTR", 128, true, 16),
    AES192CTR("AES192CTR", "AES", "CTR", 192, true, 16),
    AES256CTR("AES256CTR", "AES", "CTR", 256, true, 16),
    DES56ECB("DES56ECB", "DES", "ECB", 56, false, 0),
    DES56CBC("DES56CBC", "DES", "CBC", 56, true, 8),
    DES56CTR("DES56CTR", "DES", "CTR", 56, true, 8),
    TDES112ECB("TDES112ECB", "TripleDES", "ECB", 112, false, 0),
    TDES112CBC("TDES112CBC", "TripleDES", "CBC", 112, true, 8),
    TDES112CTR("TDES112CTR", "TripleDES", "CTR", 112, true, 8),
    TDES168ECB("TDES168ECB", "TripleDES", "ECB", 168, false, 0),
    TDES168CBC("TDES168CBC", "TripleDES", "CBC", 168, true, 8),
    TDES168CTR("TDES168CTR", "TripleDES", "CTR", 168, true, 8);

    // 算法名称
    private String value;
    // 算法类型
    private String name;
    // 算法模式
    private String mode;
    // 加密位数
    private int securityLength;
    // 是否需要向量
    private boolean needIv;
    // 向量要求长度
    private int ivLength;

    static {
        for (SecurityAlgorithm algorithm : values()) {
            SecurityUtils.registerSecurityAlgorithm(algorithm);
        }
    }

    BasicSecurityAlgorithm(String value, String name, String mode, int securityLength, boolean needIv, int ivLength) {
        this.value = value;
        this.name = name;
        this.mode = mode;
        this.securityLength = securityLength;
        this.needIv = needIv;
        this.ivLength = ivLength;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public int getSecurityLength() {
        return securityLength;
    }

    @Override
    public boolean isNeedIv() {
        return needIv;
    }

    @Override
    public int getIvLength() {
        return ivLength;
    }
}
