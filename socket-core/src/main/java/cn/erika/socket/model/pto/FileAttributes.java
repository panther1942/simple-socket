package cn.erika.socket.model.pto;

import java.io.Serializable;
import java.util.Date;

// 记录文件基本属性
public class FileAttributes implements Serializable {
    private static final long serialVersionUID = 1L;
    // 文件名
    private String filename;
    // posix标志位 显示为rwx这种格式
    private String posix;
    // 文件属主
    private String owner;
    // 文件属组
    private String group;
    // 文件长度
    private long length;
    // 上次修改时间
    private Date lastModifiedTime;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPosix() {
        return posix;
    }

    public void setPosix(String posix) {
        this.posix = posix;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public String toString() {
        return "FileAttributes{" +
                "filename='" + filename + '\'' +
                ", posix='" + posix + '\'' +
                ", owner='" + owner + '\'' +
                ", group='" + group + '\'' +
                ", length=" + length +
                ", lastModifiedTime=" + lastModifiedTime +
                '}';
    }
}
