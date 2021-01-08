package cn.erika.socket.orm.impl;

import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.socket.orm.IFileTransInfoService;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.socket.orm.IFileTransRecordService;

@Component("fileTransInfoService")
public class FileTransInfoServiceImpl extends BaseService implements IFileTransInfoService {
    private IFileTransRecordService fileTransRecordService;
    private IFileTransPartRecordService fileTransPartRecordService;

    public FileTransInfoServiceImpl() throws BeanException {
        this.fileTransRecordService = getBean("fileTransRecordService");
        this.fileTransPartRecordService = getBean("fileTransPartRecordService");
    }

    @Override
    public FileTransInfo getTransInfoByRecordUid(String uuid) {
        FileTransRecord record = fileTransRecordService.getByUuid(uuid);
        return getTransInfo(record);
    }

    @Override
    public FileTransInfo getTransInfoByFilepath(String filepath) {
        FileTransRecord record = fileTransRecordService.getRecordByFilepath(filepath);
        return getTransInfo(record);
    }

    @Override
    public FileTransInfo getTransInfoBySign(String sign, String algorithm) {
        FileTransRecord record = fileTransRecordService.getRecordBySign(sign, algorithm);
        return getTransInfo(record);
    }

    private FileTransInfo getTransInfo(FileTransRecord record) {
        if (record != null) {
            FileTransInfo info = new FileTransInfo();
            info.setRecord(record);
            info.setParts(fileTransPartRecordService.getPartsByTaskId(record.getUuid()));
            return info;
        } else {
            return null;
        }
    }
}
