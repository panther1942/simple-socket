package cn.erika.socket.orm.impl;

import cn.erika.context.annotation.Component;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.orm.IFileTransRecordService;
import cn.erika.utils.db.CommonServiceImpl;

@Component("fileTransRecordService")
public class FileTransRecordServiceImpl extends CommonServiceImpl<FileTransRecord> implements IFileTransRecordService {
    private FileTransRecord dao = FileTransRecord.dao;

    @Override
    public FileTransRecord getRecordBySign(String sign, String algorithm) {
        String sql = "SELECT * FROM tb_file_trans WHERE `sign`=? AND `algorithm`=?";
        return dao.selectOne(sql, sign, algorithm);
    }

    @Override
    public FileTransRecord getRecordByFilepath(String filepath) {
        String sql = "SELECT * FROM tb_file_trans WHERE `filepath`=?";
        return dao.selectOne(sql, filepath);
    }

    @Override
    public FileTransRecord createTransRecord(String filename, String filepath, int threads, FileInfo fileInfo, String sender, String receiver) {
        FileTransRecord record = new FileTransRecord();
        record.setFilename(filename);
        record.setFilepath(filepath);
        record.setLength(fileInfo.getLength());
        record.setSign(fileInfo.getSign());
        record.setAlgorithm(fileInfo.getAlgorithm());
        record.setThreads(threads);
        record.setSender(sender);
        record.setReceiver(receiver);
        return record;
    }
}
