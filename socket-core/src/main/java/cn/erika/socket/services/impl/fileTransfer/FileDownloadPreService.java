package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Enhance;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.handler.bio.FileReceiver;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

// TODO 未完成的文件下载 思路和文件上传一样
public class FileDownloadPreService extends BaseService implements ISocketService {
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
        String filepath = message.get(Constant.FILEPATH);
        File file = new File(filepath);
        try {
            FileInfo info = FileUtils.getFileInfo(file);
            if (!file.canRead()) {
                throw new IOException("文件不可读");
            } else {
                log.info("文件路径: " + file.getAbsolutePath() + " 文件长度: " + file.length());
            }
            Message request = new Message(Constant.SRV_PRE_DOWNLOAD);
            String token = UUID.randomUUID().toString();

            request.add(Constant.FILE_INFO, info);
            request.add(Constant.TOKEN, token);
            request.add(Constant.SEND_STATUS, true);
            socket.getHandler().set(token, file.getAbsoluteFile());
            log.debug("发送预请求");
            socket.send(request);
        } catch (IOException e) {
            log.error("读取文件失败", e);
            Message request = new Message(Constant.SRV_PRE_DOWNLOAD);
            request.add(Constant.SEND_STATUS, false);
            socket.send(request);
        }
    }

    private void preDownload(ISocket socket, Message message) {
        String filepath = message.get(Constant.FILEPATH);
        // 检查本地是否有文件 而且要想想咋处理

        // 处理完如果接收 就执行下面的
        Message request = new Message(Constant.SRV_PRE_DOWNLOAD);
        request.add(Constant.FILEPATH, filepath);
        socket.send(request);
    }

    private void download(ISocket socket, Message message) {
        Boolean status = message.get(Constant.SEND_STATUS);
        if (status != null && status) {
            try {
                new FileReceiver(socket, message);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else {
            String msg = message.get(Constant.TEXT);
            log.error(msg);
        }
    }
}
