package cn.erika.socket.model.pto;

import java.io.Serializable;
import java.util.Date;

// 文件传输前发送需要的信息
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    // 文件名
    private String filename;
    // 文件长度
    private long fileLength;
    // 偏移量
    private long filePos;
    // 文件签名(CRC)
    private long checkCode;
    // 时间戳
    private Date timestamp = new Date();

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public long getFilePos() {
        return filePos;
    }

    public void setFilePos(long filePos) {
        this.filePos = filePos;
    }

    public long getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(long checkCode) {
        this.checkCode = checkCode;
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
                "filename='" + filename + '\'' +
                ", fileLength=" + fileLength +
                ", filePos=" + filePos +
                ", checkCode=" + checkCode +
                ", timestamp=" + timestamp +
                '}';
    }
}