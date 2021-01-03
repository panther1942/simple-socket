package cn.erika.utils.io.compress;

import cn.erika.utils.exception.CompressException;
import cn.erika.utils.io.compress.stream.StreamCompress;

public class NoneArchive implements StreamCompress {
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
    public byte[] uncompress(byte[] data) throws CompressException {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        return compare(o);
    }
}
