package cn.erika.socket.model.pto;

import cn.erika.socket.model.po.FileTransPartRecord;

import java.io.Serializable;
import java.util.Date;

// 文件传输前发送需要的信息
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uuid;
    // 文件名
    private String filename;
    // 文件长度
    private long length = 0;
    // 偏移量
    private long pos = 0;
    // 文件签名
    private String sign;
    // 签名类型
    private String algorithm;
    // 传输片段token
    private String partToken;
    // 片段CRC32
    private Long crc = -1L;
    // 文件完整性
    private Integer status = 0;
    // 时间戳
    private Date timestamp = new Date();

    public FileInfo() {
    }

    public FileInfo(FileTransPartRecord record) {
        this.uuid = record.getUuid();
        this.filename = record.getFilename();
        this.length = record.getLength();
        this.pos = record.getPos();
        this.crc = record.getCrc();
        this.status = record.getStatus();
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        FileInfo info = (FileInfo) object;

        if (length != info.length) {
            return false;
        }
        if (pos != info.pos) {
            return false;
        }
        if (!filename.equals(info.filename)) {
            return false;
        }
        return crc.equals(info.crc);
    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + (int) (length ^ (length >>> 32));
        result = 31 * result + (int) (pos ^ (pos >>> 32));
        result = 31 * result + crc.hashCode();
        return result;
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
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }
}
