package cn.erika.socket.model.pto;

import java.io.Serializable;
import java.util.Date;

// 文件传输前发送需要的信息
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uuid;
    // 文件名
    private String filename;
    // 文件长度
    private long length;
    // 偏移量
    private long pos;
    // 文件签名
    private String sign;
    // 签名类型
    private String algorithm;
    // 传输片段token
    private String partToken;
    // 片段CRC32
    private Long crc;
    // 时间戳
    private Date timestamp = new Date();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getPartToken() {
        return partToken;
    }

    public void setPartToken(String partToken) {
        this.partToken = partToken;
    }

    public Long getCrc() {
        return crc;
    }

    public void setCrc(Long crc) {
        this.crc = crc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "uuid='" + uuid + '\'' +
                ", filename='" + filename + '\'' +
                ", length=" + length +
                ", pos=" + pos +
                ", sign='" + sign + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", partToken='" + partToken + '\'' +
                ", crc=" + crc +
                ", timestamp=" + timestamp +
                '}';
    }
}
