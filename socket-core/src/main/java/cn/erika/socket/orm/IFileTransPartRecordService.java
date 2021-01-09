package cn.erika.socket.orm;

import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.utils.db.ICommonService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IFileTransPartRecordService extends ICommonService<FileTransPartRecord> {
    public List<FileTransPartRecord> getPartsByTaskId(String taskId);

    List<FileInfo> getFileInfoList(List<FileTransPartRecord> partList);

    List<FileInfo> getFileInfoList(File file, String filename, int threads) throws IOException;

    List<FileTransPartRecord> createPartInfo(List<FileInfo> fileInfoList, FileTransRecord record) throws IOException;

    boolean checkPartInfo(List<FileInfo> src, List<FileInfo> dest);
}
