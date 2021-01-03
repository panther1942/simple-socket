package cn.erika.socket.core.component;

import java.io.Serializable;
import java.util.Date;

public class FileAttributes implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filename;
    private String posix;
    private String owner;
    private String group;
    private long length;
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
