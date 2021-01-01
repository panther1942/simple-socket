package cn.erika.utils.io.compress;

import cn.erika.utils.exception.CompressException;

public class NoneArchive implements CompressAlgorithm {
    public static final String NAME = "NONE";
    public static final int CODE = 0x00;

    static {
        CompressUtils.register(new NoneArchive());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getCode() {
        return CODE;
    }

    @Override
    public byte[] compress(byte[] data) throws CompressException {
        return data;
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressException {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CompressAlgorithm)) {
            return false;
        }
        return CompressUtils.compare(this, (CompressAlgorithm) o);
    }
}
