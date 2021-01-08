package cn.erika.socket.orm;

import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.utils.db.ICommonService;

public interface IFileTransRecordService extends ICommonService<FileTransRecord> {

    public FileTransRecord getRecordBySign(String sign, String algorithm);

    public FileTransRecord getRecordByFilepath(String filepath);

    FileTransRecord createTransRecord(String filename, String filepath, int threads, FileInfo fileInfo, String sender, String receiver);
}
