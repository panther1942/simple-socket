package cn.erika.socket.core.component;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据包 负责最底层的通信
 * 数据完整性校验由Message去做了
 */
public class DataInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    // 数据头部长度
    public static final int LEN = 13 + 2 + 10 + 10 + 32;

    // 时间戳13字节
    private Date timestamp;
    // 是否压缩2字节 16进制
    private int compress = 0x00;
    // 偏移量10字节
    private long pos;
    // 长度10字节
    private int len;
    // 签名 MD5 16
    private String sign;
    // 数据和数据头分两次发送 因为数据体长度不固定
    private byte[] data;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getCompress() {
        return compress;
    }

    public void setCompress(int compress) {
        this.compress = compress;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
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
        String timestamp = String.format("%13s", this.timestamp.getTime()).replaceAll("\\s", "0");
        String compress = String.format("%2x", this.compress).replaceAll("\\s", "0");
        String pos = String.format("%10s", this.pos).replaceAll("\\s", "0");
        String len = String.format("%10s", this.len).replaceAll("\\s", "0");
        String sign = String.format("%32s", this.sign);
        return timestamp + compress + pos + len + sign;
    }
}
