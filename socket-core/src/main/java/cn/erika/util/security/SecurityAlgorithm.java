package cn.erika.util.security;

public interface SecurityAlgorithm {
    String getValue();

    String getName();

    String getMode();

    int getSecurityLength();

    boolean isNeedIv();

    int getIvLength();
}
