package cn.erika.socket.service.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.component.FileInfo;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.service.ISocketService;
import cn.erika.util.security.MessageDigest;
import cn.erika.util.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@SocketServiceMapping(Constant.SRV_POST_UPLOAD)
public class FileUploadPostService implements ISocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    public void client(BaseSocket socket, Message message) {
        String msg = message.get(Constant.MESSAGE);
        log.info(msg);
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        socket.close();
        log.info("传输完成 正在进行数据校验");
        try {
            String sessionToken = socket.get(Constant.SESSION_TOKEN);
            FileInfo info = App.get(sessionToken);
            String filename = info.getFilename();
            MessageDigest.Type algorithmSign = info.getAlgorithmSign();
            byte[] sign = info.getSign();
            String targetSign = MessageDigest.byteToHexString(sign);
            File file = new File(BASE_DIR + filename);
            log.info("文件位置: " + file.getAbsolutePath());
            String currentSign = MessageDigest.byteToHexString(
                    MessageDigest.sum(file, algorithmSign)
            );
            BaseSocket parent = socket.get(Constant.PARENT_SOCKET);
            if (targetSign.equalsIgnoreCase(currentSign)) {
                log.info("数据完整");
                parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收完成"));
            } else {
                log.warn("数据不完整\n预期值: " + targetSign + "\n实际值: " + currentSign);
                parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收失败"));
            }
        } catch (BeanException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
