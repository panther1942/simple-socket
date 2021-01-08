package cn.erika.socket.orm.impl;

import cn.erika.context.annotation.Component;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.utils.db.CommonServiceImpl;

import java.util.List;

@Component("fileTransPartRecordService")
public class FileTransPartRecordServiceImpl extends CommonServiceImpl<FileTransPartRecord> implements IFileTransPartRecordService {
    private FileTransPartRecord dao = FileTransPartRecord.dao;

    @Override
    public List<FileTransPartRecord> getPartsByTaskId(String taskId) {
        String sql = "SELECT * FROM tb_file_trans_part WHERE `task_id`=?";
        return dao.select(sql, taskId);
    }

    @Override
    public FileTransPartRecord addFileTransPartRecord(FileInfo fileInfo, FileTransRecord taskRecode) {
        FileTransPartRecord recode = new FileTransPartRecord();
        recode.setFilename(fileInfo.getFilename());
        recode.setTaskId(taskRecode.getUuid());
        recode.setLength(fileInfo.getLength());
        recode.setPos(fileInfo.getPos());
        recode.setCrc(fileInfo.getCrc());
        recode.insert();
        return recode;
    }
}
