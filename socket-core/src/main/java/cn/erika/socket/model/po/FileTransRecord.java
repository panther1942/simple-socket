package cn.erika.socket.model.po;

import cn.erika.utils.db.Entry;
import cn.erika.utils.db.annotation.Column;
import cn.erika.utils.db.annotation.Table;
import cn.erika.utils.db.format.DateFormat;

import java.util.Date;

@Table("tb_file_trans")
public class FileTransRecord extends Entry<FileTransRecord> {
    public static final FileTransRecord dao = new FileTransRecord();

    @Column(primary = true)
    private String uuid;
    private String filename;
    private String filepath;
    private Long length;
    private String sign;
    private String algorithm;
    private Integer threads;
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

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
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

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
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
        return "FileTransRecord{" +
                "uuid='" + uuid + '\'' +
                ", filename='" + filename + '\'' +
                ", filepath='" + filepath + '\'' +
                ", length=" + length +
                ", sign='" + sign + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", threads=" + threads +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
