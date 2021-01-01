package cn.erika.util;

import cn.erika.utils.security.SecurityAlgorithm;
import cn.erika.utils.security.SecurityUtils;

public enum SecurityAlgorithmExtra implements SecurityAlgorithm {
    AES128OFB("AES128OFB", "AES", "OFB", 128, true, 16),
    AES128CFB("AES128CFB", "AES", "CFB", 128, true, 16),
    AES128GCM("AES128GCM", "AES", "GCM", 128, true, 16);

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

    SecurityAlgorithmExtra(String value, String name, String mode, int securityLength, boolean needIv, int ivLength) {
        this.value = value;
        this.name = name;
        this.mode = mode;
        this.securityLength = securityLength;
        this.needIv = needIv;
        this.ivLength = ivLength;
    }

    @Override
    public String getValue() {
        return value;
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

