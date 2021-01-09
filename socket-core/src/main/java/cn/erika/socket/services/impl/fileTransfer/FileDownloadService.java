package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.exception.UnsupportedAlgorithmException;
import cn.erika.utils.io.FileUtils;
import cn.erika.utils.security.MessageDigestUtils;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.string.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component(Constant.SRV_DOWNLOAD)
public class FileDownloadService extends BaseService implements ISocketService {

    @Override
    public void client(ISocket socket, Message message) throws Throwable {
        String localFile = socket.get(Constant.LOCAL_FILE);
        String fileToken = socket.get(Constant.TOKEN);
        FileInfo fileInfo = socket.get(Constant.FILE_INFO);
        FileInfo partInfo = socket.get(Constant.FILE_PART_INFO);
        String partFile = partInfo.getFilename();

        if (message.get(Constant.SERVICE_NAME) == null) {
            String remoteFile = message.get(Constant.REMOTE_FILE);
            Message request = new Message(Constant.SRV_DOWNLOAD);
            request.add(Constant.TOKEN, partInfo.getPartToken());
            request.add(Constant.REMOTE_FILE, remoteFile);
            socket.send(request);
        } else {
            long partLen = partInfo.getLength();
            long filePos = message.get(Constant.FILE_POS);
            int len = message.get(Constant.LEN);
            byte[] data = StringUtils.hexString2Byte(message.get(Constant.BIN));
            try {
                FileUtils.receiveFile(socket, partFile, partLen, filePos, len, data);
                log.info("接收完成: " + partFile);
                try {
                    if (filePos + len >= partLen) {
                        log.info("传输完成 正在进行数据校验: " + new File(partFile).getAbsolutePath());
                        if (FileUtils.checkFilePart(socket, partInfo)) {
                            mergeFile(socket, fileToken, localFile, partFile, fileInfo);
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

    @Override
    public void server(ISocket socket, Message message) throws IOException {
        String token = message.get(Constant.TOKEN);
        FileInfo fileInfo = socket.getHandler().get(token);
        String localFile = fileInfo.getFilename();
        String remoteFile = message.get(Constant.REMOTE_FILE);
        log.info("发送文件: " + localFile);
        FileUtils.sendFile(socket, Constant.SRV_DOWNLOAD, remoteFile, fileInfo);
        log.info("发送完成: " + localFile);
        Message request = new Message(Constant.SRV_POST_DOWNLOAD);
        request.add(Constant.SEND_STATUS, "发送完成: " + remoteFile);
        request.add(Constant.UID, fileInfo.getUuid());
        socket.send(request);
    }

    private void mergeFile(ISocket socket, String fileToken, String filename, String partName, FileInfo fileInfo) throws IOException, UnsupportedAlgorithmException {
        List<FileInfo> fileInfoList = socket.get(fileToken);
        int flag = 0;
        for (FileInfo partInfo : fileInfoList) {
            if (partInfo.getFilename().equals(partName)) {
                partInfo.setStatus(1);
            }
            flag += partInfo.getStatus();
        }
        File file = new File(filename);
        if (flag == fileInfoList.size()) {
            FileUtils.mergeFile(file, fileInfoList);

            String algorithm = fileInfo.getAlgorithm();
            String sign = StringUtils.byte2HexString(MessageDigestUtils.sum(
                    file, SecurityUtils.getMessageDigestAlgorithmByValue(algorithm)));
            if (sign.equals(fileInfo.getSign())) {
                log.info("文件完整");
            } else {
                log.warn("文件不完整");
            }
        }
    }
}
