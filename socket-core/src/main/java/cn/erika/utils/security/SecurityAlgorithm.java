package cn.erika.utils.security;

public interface SecurityAlgorithm {
    // 对称加密算法名称
    String getValue();

    // 对称加密算法类型
    String getName();

    // 对称加密算法模式
    String getMode();

    // 对称加密算法长度
    int getSecurityLength();

    // 对称加密算法是否需要向量
    boolean isNeedIv();

    // 对称加密算法向量长度(区块大小)
    int getIvLength();
}
