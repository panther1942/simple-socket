package cn.erika.socket.model.po;

import cn.erika.utils.db.Entry;
import cn.erika.utils.db.annotation.Column;
import cn.erika.utils.db.annotation.Table;
import cn.erika.utils.db.format.DateFormat;

import java.util.Date;

@Table("tb_file_trans")
public class FileTransRecode extends Entry<FileTransRecode> {
    public static FileTransRecode dao = new FileTransRecode();

    @Column(primary = true)
    private String uuid;
    private String filename;
    private String filepath;
    @Column("file_length")
    private Long fileLength;
    @Column("file_pos")
    private Long filePos;
    private Long crc;
    private String sender;
    private String receiver;
    @Column(value = "create_time", format = DateFormat.class)
    private Date createTime;
    @Column(value = "update_time", format = DateFormat.class)
    private Date updateTime;

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

    public long getFilePos() {
        return filePos;
    }

    public void setFilePos(long filePos) {
        this.filePos = filePos;
    }

    public Long getCrc() {
        return crc;
    }

    public void setCrc(Long crc) {
        this.crc = crc;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
