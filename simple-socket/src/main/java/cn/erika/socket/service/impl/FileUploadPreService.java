package cn.erika.socket.service.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.component.FileInfo;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.exception.FileException;
import cn.erika.socket.exception.TokenException;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.impl.FileSender;
import cn.erika.socket.service.ISocketService;
import cn.erika.util.security.MessageDigest;
import cn.erika.util.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/*

文件传输比较麻烦
首先客户端向服务器发送文件信息REQ_UPLOAD
包括文件完整路径，文件名，文件长度还有签名等信息
服务端检查下载目录下是否有同名文件 有的话检查文件大小和签名是否一致
如果一致则拒绝传输 否则请求断点续传 如果没有则将这些信息直接返回来
要加个便宜量作为 续传的信息
客户端收到发送文件的信息后新开个socket连接服务器 并进行验证操作
验证完成之后发送文件BIN 服务端验证身份后接收文件 多线程接收以后再整
接收完成后检查签名和信息头是否一致
如果文件较大则按照配置检查前100M的文件签名或者检查整个文件签名

现在的问题是如何在验证身份成功后发送文件 因为获取验证结果和发送文件之间没有直接的关系

现在的思路是整个任务
在认证就绪后执行要做的任务

以后可以加上 自动登录什么的
AOP搞起来也是为了这个

 */
@SocketServiceMapping(Constant.SRV_PRE_UPLOAD)
public class FileUploadPreService implements ISocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    public void client(BaseSocket socket, Message message) {
        if (message == null) {
            preUpload(socket);
        } else {
            upload(socket, message);
        }

    }

    @Override
    public void server(BaseSocket socket, Message message) {
        String sessionToken = UUID.randomUUID().toString();
        log.info("准备接收客户端发送文件: " + message.toString());
        try {
            FileInfo info = message.get(Constant.FILE_INFO);
            String filename = info.getFilename();
            File baseDir = new File(BASE_DIR);
            File file = new File(BASE_DIR + filename);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            if (!file.canWrite()) {
                throw new IOException("文件没有写权限");
            }
            try {
                // 考虑一下把文件的元信息记录一下
                App.add(sessionToken, message.get(Constant.FILE_INFO));
                IServer server = App.getBean(IServer.class);
                // TODO 检查本地下载目录下是否有该文件
                // 如果有 则校验完整性 1、比较文件大小 2、比较文件签名 3、数据库查询状态标志（可选）
                // 如果没有或者校验失败 检查当前目录是否可写/文件是否可写 发送准备好的信号
                // 如果校验正确（文件完整） 或者 目录或文件不可写 则发出拒绝信号
                if (true) {
                    // 检查通过 接收的话
                    server.addToken(socket, sessionToken);
                    message.add(Constant.SESSION_TOKEN, sessionToken);
                    message.add(Constant.RECEIVE_STATUS, Constant.SUCCESS);
                    // 先不管断点续传
                    message.add(Constant.FILE_POS, 0L);
                } else {
                    message.add(Constant.RECEIVE_STATUS, Constant.FAILED);
                }
                socket.send(message);
            } catch (BeanException e) {
                e.printStackTrace();
            } catch (TokenException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void preUpload(BaseSocket socket) {
        try {
            MessageDigest.Type fileSignAlgorithm = GlobalSettings.fileSignAlgorithm;
            String filename = App.pop(Constant.FILENAME);
            String filepath = App.pop(Constant.FILEPATH);
            try {
                File file = new File(filepath);
                if (!file.exists()) {
                    throw new FileException("文件不存在");
                } else if (!file.canRead()) {
                    throw new FileException("文件不可读");
                } else {
                    log.info("文件完整路径: " + file.getAbsolutePath() + " 文件名: " + filename + " 文件长度: " + file.length());
                }
                log.info("计算文件签名");
                byte[] sign = MessageDigest.sum(file, fileSignAlgorithm);
                log.info("文件签名: " + MessageDigest.byteToHexString(sign));

                Message request = new Message(Constant.SRV_PRE_UPLOAD);
                FileInfo info = new FileInfo();
                info.setFilename(filename);
                info.setFilepath(file.getAbsolutePath());
                info.setFileLength(file.length());
                info.setAlgorithmSign(fileSignAlgorithm);
                info.setSign(sign);
                request.add(Constant.FILE_INFO, info);

                socket.send(request);
            } catch (SecurityException e) {
                log.error("当前系统不支持这种签名算法: " + fileSignAlgorithm.getValue());
            } catch (FileException | IOException e) {
                log.error("读取文件失败");
            }
        } catch (BeanException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void upload(BaseSocket socket, Message message) {
        String status = message.get(Constant.RECEIVE_STATUS);
        if (Constant.SUCCESS.equals(status)) {
            try {
                new FileSender(socket, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
