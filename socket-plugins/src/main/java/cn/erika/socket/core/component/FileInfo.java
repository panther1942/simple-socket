package cn.erika.socket.core.component;

import java.io.Serializable;

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
}
