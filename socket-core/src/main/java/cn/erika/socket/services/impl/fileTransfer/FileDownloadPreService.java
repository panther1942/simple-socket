package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.context.annotation.Inject;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.handler.bio.FileReceiver;
import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.socket.orm.IFileTransInfoService;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.socket.orm.IFileTransRecordService;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.exception.UnsupportedAlgorithmException;
import cn.erika.utils.io.FileUtils;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.security.MessageDigestUtils;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.string.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component(Constant.SRV_PRE_DOWNLOAD)
public class FileDownloadPreService extends BaseService implements ISocketService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject(name = "fileTransPartRecordService")
    private IFileTransPartRecordService transPartService;

    @Override
    public void client(ISocket socket, Message message) {
        if (message.get(Constant.SERVICE_NAME) == null) {
            preDownload(socket, message);
        } else {
            download(socket, message);
        }
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void server(ISocket socket, Message message) {
        String remoteFile = message.get(Constant.REMOTE_FILE);
        FileInfo remoteFileInfo = message.get(Constant.FILE_INFO);
        Integer threads = message.get(Constant.THREADS);
        // 当前目录
        String pwd = socket.get(Constant.PWD);
        // 最终文件保存路径
        File file = null;
        if (remoteFile.startsWith(FileUtils.SYS_FILE_SEPARATOR)) {
            file = new File(remoteFile);
        } else {
            file = new File(pwd + FileUtils.SYS_FILE_SEPARATOR + remoteFile);
        }
        try {
            if (!file.exists()) {
                throw new IOException("文件不存在: " + file.getAbsolutePath());
            }
            FileInfo localFileInfo = FileUtils.getFileInfo(file);
            if (localFileInfo.equals(remoteFileInfo)) {
                throw new IOException("文件完整");
            }
            if (threads == null || threads < 1 || threads > GlobalSettings.threadsLimit) {
                threads = GlobalSettings.threads;
            }
            IServer server = getBean(IServer.class);
            List<FileInfo> fileInfoList = transPartService.getFileInfoList(file, localFileInfo.getFilename(), threads);
            for (FileInfo fileInfo : fileInfoList) {
                String partToken = UUID.randomUUID().toString();
                fileInfo.setPartToken(partToken);
                socket.getHandler().add(partToken, fileInfo);
                server.addToken(socket, partToken);
            }
            message.add(Constant.REMOTE_FILE, file.getAbsolutePath());
            message.add(Constant.FILE_PART_INFO, fileInfoList);
            message.add(Constant.FILE_INFO, localFileInfo);
            message.add(Constant.SEND_STATUS, true);
            socket.send(message);
        } catch (IOException e) {
            log.error("读取文件失败", e);
            Message request = new Message(Constant.SRV_PRE_DOWNLOAD);
            request.add(Constant.SEND_STATUS, false);
            socket.send(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preDownload(ISocket socket, Message message) {
        String localFile = message.get(Constant.LOCAL_FILE);
        String remoteFile = message.get(Constant.REMOTE_FILE);
        File file = new File(localFile);
        FileInfo fileInfo = null;
        try {
            if (file.exists()) {
                fileInfo = FileUtils.getFileInfo(file);
            }
            String fileToken = UUID.randomUUID().toString();
            socket.set(fileToken, file.getAbsolutePath());
            Message request = new Message(Constant.SRV_PRE_DOWNLOAD);
            request.add(Constant.REMOTE_FILE, remoteFile);
            request.add(Constant.FILE_INFO, fileInfo);
            request.add(Constant.TOKEN, fileToken);
            request.add(Constant.THREADS, GlobalSettings.threads);
            socket.send(request);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    private void download(ISocket socket, Message message) {
        String fileToken = message.get(Constant.TOKEN);
        String localFile = socket.get(fileToken);
        String remoteFile = message.get(Constant.REMOTE_FILE);
        List<FileInfo> fileInfoList = message.get(Constant.FILE_PART_INFO);
        FileInfo fileInfo = message.get(Constant.FILE_INFO);
        Boolean status = message.get(Constant.SEND_STATUS);
        try {
            if (status != null && status) {
                if (fileInfoList == null) {
                    throw new IOException("缺少文件分组信息");
                } else {
                    socket.set(fileToken, fileInfoList);
                }
                int count = 0;
                for (FileInfo partInfo : fileInfoList) {
                    File partFile = new File(localFile.substring(0, localFile.lastIndexOf(
                            FileUtils.SYS_FILE_SEPARATOR)) + partInfo.getFilename());
                    partInfo.setFilename(partFile.getAbsolutePath());
                    if (partFile.exists() && MessageDigestUtils.crc32Sum(partFile) == partInfo.getCrc()) {
                        log.info("片段完整: " + partFile);
                        count++;
                        continue;
                    } else if (partFile.exists() && !partFile.delete()) {
                        throw new IOException("无法删除文件片段: " + partFile.getAbsolutePath());
                    }
                    new FileReceiver(socket, localFile, remoteFile, fileInfo, partInfo, fileToken);
                }
                if (count == fileInfoList.size()) {
                    FileUtils.mergeFile(new File(localFile), fileInfoList);
                }
            } else {
                String msg = message.get(Constant.TEXT);
                log.error(msg);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
