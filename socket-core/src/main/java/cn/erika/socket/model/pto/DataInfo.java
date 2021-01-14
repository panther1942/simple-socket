package cn.erika.socket.model.pto;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据包 负责最底层的通信
 * 数据完整性校验由Message去做了
 */
public class DataInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    // 数据头部长度
    public static final int LEN = 2 + 10;
    // 是否压缩 2字节 16进制
    private int compress = 0x00;
    // 长度 10字节
    private int len;
    // 数据和数据头分两次发送 因为数据体长度不固定
    private byte[] data;

    public int getCompress() {
        return compress;
    }

    public void setCompress(int compress) {
        this.compress = compress;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    // 避免平台差异导致解析出错 故在此直接定死数据头格式 保证一致性
    @Override
    public String toString() {
        String compress = String.format("%2x", this.compress).replaceAll("\\s", "0");
        String len = String.format("%10s", this.len).replaceAll("\\s", "0");
        return new StringBuffer(compress).append(len).toString();
    }
}
