package cn.erika.utils.io.compress;

import cn.erika.utils.exception.CompressException;

public interface CompressAlgorithm {

    public String getName();

    public int getCode();

    public byte[] compress(byte[] data) throws CompressException;

    public byte[] decompress(byte[] data) throws CompressException;
}
