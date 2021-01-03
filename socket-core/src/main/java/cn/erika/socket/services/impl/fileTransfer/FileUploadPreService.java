package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.FileInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.bio.FileSender;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component(Constant.SRV_PRE_UPLOAD)
public class FileUploadPreService extends BaseService implements ISocketService {
//    upload /home/erika/Downloads/config.json config123.json

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void client(ISocket socket, Message message) {
        if (message.get(Constant.SERVICE_NAME) == null) {
            preUpload(socket, message);
        } else {
            upload(socket, message);
        }
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void server(ISocket socket, Message message) {
        String token = message.get(Constant.TOKEN);
        try {
            FileInfo info = message.get(Constant.FILE_INFO);
            String pwd = socket.get(Constant.PWD);
            String filename = info.getFilename();
            log.info("准备接收客户端发送文件: " + filename);
            // TODO 检查本地下载目录下是否有该文件
            // 如果有 则校验完整性 1、比较文件大小 2、比较文件签名 3、数据库查询状态标志（可选）
            // 如果没有或者校验失败 检查当前目录是否可写/文件是否可写 发送准备好的信号
            // 如果校验正确（文件完整） 或者 目录或文件不可写 则发出拒绝信号
            File file = new File(pwd + FileUtils.SYS_FILE_SEPARATOR + filename);
            if (file.exists()) {
                // 如果文件存在则需要判断(根据策略)覆盖源文件还是检查不完整后续传
                // 先不管断点续传
                info.setFilePos(0);
                info.setFilename(file.getAbsolutePath());
                FileUtils.createFile(file);
            } else {
                info.setFilePos(0);
                info.setFilename(file.getAbsolutePath());
                FileUtils.createFile(file);
            }
            if (!file.canWrite()) {
                throw new IOException("文件没有写权限");
            }
            // 考虑一下把文件的元信息记录一下
            socket.getHandler().set(token, info);
            IServer server = getBean(IServer.class);
            server.addToken(socket, token);
            message.add(Constant.TOKEN, token);
            message.add(Constant.RECEIVE_STATUS, true);
            socket.send(message);
        } catch (Exception e) {
            log.warn(e.getMessage());
            message.add(Constant.RECEIVE_STATUS, false);
            message.add(Constant.TEXT, e.getMessage());
            socket.send(message);
        }
    }

    private void preUpload(ISocket socket, Message message) {
        String filename = message.get(Constant.FILENAME);
        String filepath = message.get(Constant.FILEPATH);
        try {
            File file = new File(filepath);
            FileInfo info = FileUtils.getFileInfo(file);
            if (!file.canRead()) {
                throw new IOException("文件不可读: " + file.getAbsolutePath());
            } else {
                info.setFilename(filename);
                log.info("文件路径: " + file.getAbsolutePath() + " 文件名: " + filename + " 文件长度: " + file.length());
            }
            Message request = new Message(Constant.SRV_PRE_UPLOAD);
            String token = UUID.randomUUID().toString();

            request.add(Constant.FILE_INFO, info);
            request.add(Constant.TOKEN, token);
            socket.set(token, file.getAbsolutePath());
            log.debug("发送预请求");
            socket.send(request);
        } catch (IOException e) {
            log.error("读取文件失败", e);
        }
    }

    private void upload(ISocket socket, Message message) {
        Boolean status = message.get(Constant.RECEIVE_STATUS);
        if (status != null && status) {
            try {
                new FileSender(socket, message);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else {
            String token = message.get(Constant.TOKEN);
            socket.remove(token);
        }
    }
}
