package cn.erika.socket.model.po;

import cn.erika.socket.model.pto.FileInfo;
import cn.erika.utils.db.Entry;
import cn.erika.utils.db.annotation.Column;
import cn.erika.utils.db.annotation.Table;
import cn.erika.utils.db.format.DateFormat;

import java.util.Date;

@Table("tb_file_trans_part")
public class FileTransPartRecord extends Entry<FileTransPartRecord> {
    public static final FileTransPartRecord dao = new FileTransPartRecord();

    @Column(primary = true)
    private String uuid;
    @Column("task_id")
    private String taskId;
    private String filename;
    private Long length;
    private Long pos;
    private Long crc;
    private Integer status;
    @Column(value = "create_time", format = DateFormat.class)
    private Date createTime;
    @Column(value = "update_time", format = DateFormat.class)
    private Date updateTime;

    public FileTransPartRecord() {
    }

    public FileTransPartRecord(String taskId, FileInfo fileInfo) {
        this.taskId = taskId;
        this.filename = fileInfo.getFilename();
        this.length = fileInfo.getLength();
        this.pos = fileInfo.getPos();
        this.crc = fileInfo.getCrc();
        this.status = 0;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Long getPos() {
        return pos;
    }

    public void setPos(Long pos) {
        this.pos = pos;
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

    @Override
    public int insert() {
        this.createTime = new Date();
        this.updateTime = new Date();
        return super.insert();
    }

    @Override
    public int update() {
        this.updateTime = new Date();
        return super.update();
    }

    @Override
    public String toString() {
        return "FileTransPartRecord{" +
                "uuid='" + uuid + '\'' +
                ", taskId='" + taskId + '\'' +
                ", filename='" + filename + '\'' +
                ", length=" + length +
                ", pos=" + pos +
                ", crc=" + crc +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
