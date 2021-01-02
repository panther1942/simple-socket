package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.Application;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.FileInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.socket.exception.FileException;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.bio.FileSender;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.security.MessageDigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component(Constant.SRV_PRE_UPLOAD)
public class FileUploadPreService extends BaseService implements ISocketService {
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    public void client(ISocket socket, Message message) {
        if (message.get(Constant.SERVICE_NAME) == null) {
            preUpload(socket, message);
        } else {
            upload(socket, message);
        }
    }

    @Override
    public void server(ISocket socket, Message message) {
        String token = message.get(Constant.TOKEN);
        try {
            FileInfo info = message.get(Constant.FILE_INFO);
            String filename = info.getFilename();
            log.info("准备接收客户端发送文件: " + filename);
            File baseDir = new File(BASE_DIR);
            File file = new File(BASE_DIR + filename);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            if (!file.canWrite()) {
                throw new IOException("文件没有写权限");
            }
            try {
                // 考虑一下把文件的元信息记录一下
                socket.getHandler().set(token, info);
                // TODO 检查本地下载目录下是否有该文件
                // 如果有 则校验完整性 1、比较文件大小 2、比较文件签名 3、数据库查询状态标志（可选）
                // 如果没有或者校验失败 检查当前目录是否可写/文件是否可写 发送准备好的信号
                // 如果校验正确（文件完整） 或者 目录或文件不可写 则发出拒绝信号
                if (true) {
                    // 检查通过 接收的话
                    IServer server = getBean(IServer.class);
                    server.addToken(socket, token);
                    message.add(Constant.TOKEN, token);
                    message.add(Constant.RECEIVE_STATUS, true);
                    // 先不管断点续传
                    info.setFilePos(0);
                } else {
                    message.add(Constant.RECEIVE_STATUS, false);
                }
                socket.send(message);
            } catch (BeanException e) {
                log.error("内部错误: " + e.getMessage());
            } catch (AuthenticateException e) {
                log.error("无法添加认证信息: " + e.getMessage());
            }
        } catch (IOException e) {
            log.error("本地文件读写错误: " + e.getMessage());
        }
    }

    private void preUpload(ISocket socket, Message message) {
        boolean isAuth = socket.get(Constant.AUTHENTICATED);
        if (!isAuth) {
            log.warn("还没有认证(login)");
            return;
        }
        String filename = message.get(Constant.FILENAME);
        String filepath = message.get(Constant.FILEPATH);
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileException("文件不存在: " + file.getAbsolutePath());
            } else if (!file.canRead()) {
                throw new FileException("文件不可读: " + file.getAbsolutePath());
            } else {
                log.info("文件完整路径: " + file.getAbsolutePath() + " 文件名: " + filename + " 文件长度: " + file.length());
            }
            log.info("计算文件校验码");
            long checkCode = MessageDigestUtils.crc32Sum(file);
            log.info("文件校验码: " + checkCode);

            Message request = new Message(Constant.SRV_PRE_UPLOAD);
            FileInfo info = new FileInfo();
            info.setFilename(filename);
            info.setFileLength(file.length());
            info.setCheckCode(checkCode);
            String token = UUID.randomUUID().toString();
            request.add(Constant.FILE_INFO, info);
            request.add(Constant.TOKEN, token);
            socket.set(token, file.getAbsolutePath());
            log.debug("发送预请求");
            socket.send(request);
        } catch (FileException e) {
            log.error("读取文件失败", e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upload(ISocket socket, Message message) {
        boolean status = message.get(Constant.RECEIVE_STATUS);
        if (status) {
            try {
                new FileSender(socket, message);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }
}
