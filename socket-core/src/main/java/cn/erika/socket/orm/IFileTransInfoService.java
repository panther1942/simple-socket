package cn.erika.socket.orm;

import cn.erika.socket.model.vo.FileTransInfo;

public interface IFileTransInfoService {
    public FileTransInfo getTransInfoByRecordUid(String uuid);

    public FileTransInfo getTransInfoByFilepath(String filepath);

    public FileTransInfo getTransInfoBySign(String sign, String algorithm);
}
