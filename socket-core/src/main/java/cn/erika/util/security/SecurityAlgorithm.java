package cn.erika.util.security;

public enum SecurityAlgorithm {
    AES128ECB("AES128ECB", "AES", "ECB", 128, false),
    AES192ECB("AES192ECB", "AES", "ECB", 192, false),
    AES256ECB("AES256ECB", "AES", "ECB", 256, false),
    AES128CBC("AES128CBC", "AES", "CBC", 128, true),
    AES192CBC("AES192CBC", "AES", "CBC", 192, true),
    AES256CBC("AES256CBC", "AES", "CBC", 256, true),
    AES128CTR("AES128CTR", "AES", "CTR", 128, true),
    AES192CTR("AES192CTR", "AES", "CTR", 192, true),
    AES256CTR("AES256CTR", "AES", "CTR", 256, true),
    DES56ECB("DES56ECB", "DES", "ECB", 56, false),
    DES56CBC("DES56CBC", "DES", "CBC", 56, true),
    DES56CTR("DES56CTR", "DES", "CTR", 56, true),
    TDES112ECB("TDES112ECB", "TripleDES", "ECB", 112, false),
    TDES112CBC("TDES112CBC", "TripleDES", "CBC", 112, true),
    TDES112CTR("TDES112CTR", "TripleDES", "CTR", 112, true),
    TDES168ECB("TDES168ECB", "TripleDES", "ECB", 168, false),
    TDES168CBC("TDES168CBC", "TripleDES", "CBC", 168, true),
    TDES168CTR("TDES168CTR", "TripleDES", "CTR", 168, true);

    private String value;
    private String name;
    private String mode;
    private int securityLength;
    private boolean needIv;

    SecurityAlgorithm(String value, String name, String mode, int securityLength, boolean needIv) {
        this.value = value;
        this.name = name;
        this.mode = mode;
        this.securityLength = securityLength;
        this.needIv = needIv;
    }

    public String getValue() {
        return this.value;
    }

    public String getName() {
        return name;
    }

    public String getMode() {
        return mode;
    }

    public int getSecurityLength() {
        return securityLength;
    }

    public boolean isNeedIv() {
        return needIv;
    }

    public static SecurityAlgorithm getByName(String name) {
        if (name == null) {
            return null;
        }
        for (SecurityAlgorithm algorithm : SecurityAlgorithm.values()) {
            if (algorithm.getValue().equals(name)) {
                return algorithm;
            }
        }
        return null;
    }
}
