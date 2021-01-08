package cn.erika.socket.model.vo;

import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;

import java.io.Serializable;
import java.util.List;

public class FileTransInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private FileTransRecord record;
    private List<FileTransPartRecord> parts;

    public FileTransRecord getRecord() {
        return record;
    }

    public void setRecord(FileTransRecord record) {
        this.record = record;
    }

    public List<FileTransPartRecord> getParts() {
        return parts;
    }

    public void setParts(List<FileTransPartRecord> parts) {
        this.parts = parts;
    }

    @Override
    public String toString() {
        return "FileTransInfo{" +
                "record=" + record +
                ", parts=" + parts +
                '}';
    }
}
