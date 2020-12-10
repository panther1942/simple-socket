package cn.erika.socket.common.component;

import cn.erika.util.security.MessageDigest;

import java.io.Serializable;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filename;
    private String filepath;
    private long fileLength;
    private MessageDigest.Type algorithmSign;
    private byte[] sign;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public MessageDigest.Type getAlgorithmSign() {
        return algorithmSign;
    }

    public void setAlgorithmSign(MessageDigest.Type algorithmSign) {
        this.algorithmSign = algorithmSign;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }
}
