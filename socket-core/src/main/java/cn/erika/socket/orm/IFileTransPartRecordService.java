package cn.erika.socket.orm;

import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.utils.db.ICommonService;

import java.util.List;

public interface IFileTransPartRecordService extends ICommonService<FileTransPartRecord> {
    public List<FileTransPartRecord> getPartsByTaskId(String taskId);

    public FileTransPartRecord addFileTransPartRecord(FileInfo fileInfo, FileTransRecord taskRecode);
}
