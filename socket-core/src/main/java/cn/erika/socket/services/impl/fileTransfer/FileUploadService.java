package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.aop.FileUploadTimeCount;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.orm.IFileTransInfoService;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.exception.UnsupportedAlgorithmException;
import cn.erika.utils.io.FileUtils;
import cn.erika.utils.string.StringUtils;

import java.io.*;
import java.text.DecimalFormat;

@Component(Constant.SRV_UPLOAD)
public class FileUploadService extends BaseService implements ISocketService {
    private static DecimalFormat df = new DecimalFormat("0.00%");
    private static int blockSize = GlobalSettings.fileTransBlock;

    private IFileTransInfoService fileTransInfoService;
    private IFileTransPartRecordService fileTransPartRecordService;

    public FileUploadService() throws BeanException {
        this.fileTransInfoService = getBean("fileTransInfoService");
        this.fileTransPartRecordService = getBean("fileTransPartRecordService");
    }

    @Enhance(FileUploadTimeCount.class)
    @Override
    public void client(ISocket socket, Message message) throws IOException {
        // 发送文件仅由发送端自己发起 不接受远端发起
        if (message.get(Constant.SERVICE_NAME) == null) {
            String localFile = message.get(Constant.LOCAL_FILE);
            String remoteFile = message.get(Constant.REMOTE_FILE);
            FileInfo fileInfo = message.get(Constant.FILE_INFO);
            log.info("发送文件: " + remoteFile);
            FileUtils.sendFile(socket, Constant.SRV_UPLOAD, localFile, fileInfo);
            log.info("发送完成: " + remoteFile);
            Message request = new Message(Constant.SRV_POST_UPLOAD);
            request.add(Constant.SEND_STATUS, "发送完成: " + remoteFile);
            request.add(Constant.UID, fileInfo.getUuid());
            socket.send(request);
        }
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void server(ISocket socket, Message message) {
        String token = socket.get(Constant.TOKEN);
        FileInfo fileInfo = socket.getHandler().get(token);
        String filename = fileInfo.getFilename();
        long partLen = fileInfo.getLength();
        long filePos = message.get(Constant.FILE_POS);
        int len = message.get(Constant.LEN);
        byte[] data = StringUtils.hexString2Byte(message.get(Constant.BIN));

        try {
            FileUtils.receiveFile(socket, filename, partLen, filePos, len, data);
            try {
                if (filePos + len >= partLen) {
                    log.info("传输完成 正在进行数据校验: " + new File(filename).getAbsolutePath());
                    if (FileUtils.checkFilePart(socket, fileInfo)) {
                        FileTransPartRecord filePart = fileTransPartRecordService.getByUuid(fileInfo.getUuid());
                        filePart.setStatus(1);
                        filePart.update();
                        fileTransInfoService.mergeFile(filePart.getTaskId());
                    }
                }
            } catch (IOException e) {
                log.error("校验出错: " + e.getMessage(), e);
            } catch (UnsupportedAlgorithmException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
