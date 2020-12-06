package cn.erika.socket.service.impl;

import cn.erika.config.Constant;
import cn.erika.socket.handler.impl.AbstractHandler;
import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.socket.common.exception.FileException;
import cn.erika.socket.service.ISocketService;
import cn.erika.util.security.MessageDigest;
import cn.erika.util.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
public class FileService implements ISocketService{
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void uploadRequest(TcpSocket socket, File file, String filename) throws IOException, FileException {
        if (!file.exists()) {
            throw new FileException("文件不存在");
        } else if (!file.canRead()) {
            throw new FileException("文件不可读");
        } else {
            log.info("文件完整路径: " + file.getAbsolutePath() + " 文件名: " + filename + " 文件长度: " + file.length());
        }
        new AbstractHandler() {
            @Override
            public void onOpen(BaseSocket socket) {

            }

            @Override
            public void onReady(BaseSocket socket) {
                try {
                    MessageDigest.Type algorithmSign = MessageDigest.Type.SHA384;
                    String sign = MessageDigest.byteToHexString(MessageDigest.sum(file, algorithmSign));
                    socket.send(new Message(Constant.SRV_UPLOAD_REQUEST, new HashMap<String, Object>() {
                        {
                            put(Constant.FILEPATH, file.getAbsolutePath());
                            put(Constant.FILENAME, filename);
                            put(Constant.FILE_LEN, file.length());
                            put(Constant.ALGORITHM, algorithmSign);
                            put(Constant.SIGN, sign);
                        }
                    }));
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(BaseSocket socket) {

            }

            @Override
            public void onError(String message, Throwable error) {

            }
        };
    }

    @Override
    public void client(BaseSocket socket, Message message) {

    }

    @Override
    public void server(BaseSocket socket, Message message) {

    }
}
