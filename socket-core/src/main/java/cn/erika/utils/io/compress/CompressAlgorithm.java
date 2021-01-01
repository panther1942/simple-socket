package cn.erika.utils.io.compress;

import cn.erika.utils.exception.CompressException;

public interface CompressAlgorithm {
    // 压缩算法名称
    public String getName();

    // 压缩算法代号 自己起 只要不重名 0x00为不压缩 长度不要超过两个字节(16进制字符串)
    public int getCode();

    // 压缩方法
    public byte[] compress(byte[] data) throws CompressException;

    // 解压缩方法
    public byte[] uncompress(byte[] data) throws CompressException;

    public default boolean compare(Object o) {
        if (!(o instanceof CompressAlgorithm)) {
            return false;
        }
        CompressAlgorithm that = (CompressAlgorithm) o;
        if (this.getCode() == that.getCode()) {
            return true;
        }
        if (this.getName().equalsIgnoreCase(that.getName())) {
            return true;
        }
        return false;
    }
}
