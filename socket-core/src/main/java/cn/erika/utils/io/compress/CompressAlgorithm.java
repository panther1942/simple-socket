package cn.erika.utils.io.compress;

public interface CompressAlgorithm {
    // 压缩算法名称
    public String getName();

    // 压缩算法代号 自己起 只要不重名 0x00为不压缩 长度不要超过两个字节(16进制字符串)
    public int getCode();

}
