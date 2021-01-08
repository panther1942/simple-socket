package cn.erika.socket.orm.impl;

import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.socket.orm.IFileTransInfoService;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.socket.orm.IFileTransRecordService;
import cn.erika.utils.exception.UnsupportedAlgorithmException;
import cn.erika.utils.security.MessageDigestUtils;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.string.Base64Utils;
import cn.erika.utils.string.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

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
    public synchronized void mergeFile(String taskId) throws IOException, UnsupportedAlgorithmException {
        FileTransInfo transInfo = getTransInfoByRecordUid(taskId);
        FileTransRecord transRecord = transInfo.getRecord();

        if (transRecord.getStatus() == 1) {
            return;
        }
        // 检查文件所有片段是否都传输完成
        boolean status = false;
        for (FileTransPartRecord part : transInfo.getParts()) {
            if (part.getStatus() == 1) {
                status = true;
            } else {
                status = false;
                break;
            }
        }
        // 如果传输完成 则合并文件
        if (status) {
            File target = new File(transRecord.getFilepath());
            try (RandomAccessFile writer = new RandomAccessFile(target, "rw")) {
                for (FileTransPartRecord partRecord : transInfo.getParts()) {
                    File partFile = new File(partRecord.getFilename());
                    long pos = partRecord.getPos();
                    writer.seek(pos);
                    try (FileInputStream reader = new FileInputStream(partFile)) {
                        int len = 0;
                        byte[] data = new byte[4 * 1024 * 1024];
                        while ((len = reader.read(data)) > -1) {
                            writer.write(data, 0, len);
                        }
                    }
                }
            }
            // 对文件签名
            byte[] sign = MessageDigestUtils.sum(target, SecurityUtils.getMessageDigestAlgorithmByValue(transRecord.getAlgorithm()));
            String strSign = StringUtils.byte2HexString(sign);
            // 与数据库中的数据进行对比
            if (strSign.equalsIgnoreCase(transInfo.getRecord().getSign())) {
                transRecord.setStatus(1);
                transRecord.update();
                log.info("文件完整: " + target.getAbsolutePath());
                // 如果文件完整 则删除分片文件
                for (FileTransPartRecord part : transInfo.getParts()) {
                    File file = new File(part.getFilename());
                    if (file.exists() && !file.delete()) {
                        log.warn("分片文件无法删除: " + file.getAbsolutePath());
                    }
                }
            } else {
                log.error("文件不完整: " + target.getAbsolutePath());
            }
        }
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
