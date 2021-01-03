package cn.erika.utils.io.compress.stream;

import cn.erika.utils.exception.CompressException;
import cn.erika.utils.io.compress.CompressAlgorithm;

public interface StreamCompress extends CompressAlgorithm {
    // 压缩方法
    public byte[] compress(byte[] data) throws CompressException;

    // 解压缩方法
    public byte[] uncompress(byte[] data) throws CompressException;

    public default boolean compare(Object o) {
        if (!(o instanceof StreamCompress)) {
            return false;
        }
        StreamCompress that = (StreamCompress) o;
        if (this.getCode() == that.getCode()) {
            return true;
        }
        if (this.getName().equalsIgnoreCase(that.getName())) {
            return true;
        }
        return false;
    }
}
