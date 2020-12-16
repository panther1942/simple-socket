package cn.erika.socket.component;

import cn.erika.util.security.MessageDigest;

import java.io.Serializable;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    // 文件名
    private String filename;
    // 远程文件路径 TODO 这里一定要改 能看到远程文件路径算啥情况
    private String filepath;
    // 文件长度
    private long fileLength;
    // 签名算法
    private MessageDigest.Type algorithmSign;
    // 文件签名
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
