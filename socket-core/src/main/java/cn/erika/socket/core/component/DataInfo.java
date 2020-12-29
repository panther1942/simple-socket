package cn.erika.socket.core.component;

import cn.erika.util.string.StringUtils;

import java.io.Serializable;
import java.util.Base64;
import java.util.Date;

/**
 * 数据包 负责最底层的通信
 * 数据完整性校验由Message去做了
 */
public class DataInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    // 数据头部长度
    public static final int LEN = 13 + 1 + 10 + 10 + 32;

    // 时间戳13字节
    private Date timestamp;
    // 是否压缩10字节
    private Compress compress = Compress.NONE; // 0: none 1: gzip
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

    public Compress getCompress() {
        return compress;
    }

    public void setCompress(Compress compress) {
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
        String compress = String.format("%1s", this.compress.value).replaceAll("\\s", "0");
        String pos = String.format("%10s", this.pos).replaceAll("\\s", "0");
        String len = String.format("%10s", this.len).replaceAll("\\s", "0");
        String sign = String.format("%32s", this.sign);
        return timestamp + compress + pos + len + sign;
    }

    // 数据压缩目前貌似就GZIP靠谱点
    public enum Compress {
        NONE(0),
        GZIP(1);

        private int value;

        Compress(int value) {
            this.value = value;
        }

        public static Compress getByValue(int value) {
            for (Compress compress : Compress.values()) {
                if (compress.value == value) {
                    return compress;
                }
            }
            return null;
        }
    }
}
