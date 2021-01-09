package cn.erika.socket.orm;

import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.utils.exception.UnsupportedAlgorithmException;

import java.io.IOException;

public interface IFileTransInfoService {
    public FileTransInfo getTransInfoByRecordUid(String uuid);

    public FileTransInfo getTransInfoByFilepath(String filepath);

    public void mergeFile(String taskId) throws IOException, UnsupportedAlgorithmException;
}
