package cn.erika.socket.orm.impl;

import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.utils.db.CommonServiceImpl;
import cn.erika.utils.io.FileUtils;
import cn.erika.utils.security.MessageDigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
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
    public List<FileInfo> getFileInfoList(List<FileTransPartRecord> partList) {
        List<FileInfo> fileInfoList = new LinkedList<>();
        for (FileTransPartRecord part : partList) {
            fileInfoList.add(new FileInfo(part));
        }
        return fileInfoList;
    }

    @Override
    public List<FileInfo> getFileInfoList(File file, String filename, int threads) throws IOException {
        List<FileInfo> fileInfoList = new LinkedList<>();
        // 分片的保存文件名
        filename = FileUtils.SYS_FILE_SEPARATOR + filename + Constant.FILE_PART_POSTFIX;
        // 文件长度
        long fileLength = file.length();
        // 分片长度
        long partLength = fileLength / threads;
        // 创建分片信息
        for (int i = 0; i < threads; i++) {
            FileInfo partInfo = new FileInfo();
            // 文件名依次加1
            partInfo.setFilename(filename + i);
            // 分片长度
            partInfo.setLength(partLength);
            // 偏移量
            partInfo.setPos(i * partLength);
            // 最后一个分片的长度等于剩余的全部长度
            if (i == threads - 1) {
                partInfo.setLength(fileLength - partLength * i);
            }
            partInfo.setCrc(MessageDigestUtils.crc32Sum(file, partInfo.getPos(), partInfo.getLength()));
            fileInfoList.add(partInfo);
        }
        return fileInfoList;
    }

    @Override
    public List<FileTransPartRecord> createPartInfo(List<FileInfo> fileInfoList, FileTransRecord record) throws IOException {
        List<FileTransPartRecord> parts = new LinkedList<>();
        for (FileInfo fileInfo : fileInfoList) {
            FileTransPartRecord part = new FileTransPartRecord(record.getUuid(), fileInfo);
            if (part.insert() > 0) {
                fileInfo.setUuid(part.getUuid());
                parts.add(part);
            } else {
                throw new IOException("新增文件片段记录失败");
            }
        }
        return parts;
    }

    @Override
    public boolean checkPartInfo(List<FileInfo> src, List<FileInfo> dest) {
        if (src == null || dest == null) {
            return false;
        }
        if (src.size() != dest.size()) {
            return false;
        }
        for (int i = 0; i < src.size(); i++) {
            if (!src.get(i).equals(dest.get(i))) {
                return false;
            }
        }
        return true;
    }
}
