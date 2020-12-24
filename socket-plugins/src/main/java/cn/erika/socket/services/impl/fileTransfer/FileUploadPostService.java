package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.Application;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.FileInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.SocketService;
import cn.erika.util.security.MessageDigest;

import java.io.File;
import java.io.IOException;

@Component(Constant.SRV_POST_UPLOAD)
public class FileUploadPostService extends BaseService implements SocketService {
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    public void client(Socket socket, Message message) {
        String msg = message.get(Constant.TEXT);
        log.info(msg);
    }

    @Override
    public void server(Socket socket, Message message) {
        socket.close();
        log.info("传输完成 正在进行数据校验");
        try {
            String token = socket.get(Constant.TOKEN);
            FileInfo info = Application.get(token);
            String filename = info.getFilename();
            long checkCode = info.getCheckCode();
            File file = new File(BASE_DIR + filename);
            log.info("文件位置: " + file.getAbsolutePath());
            BaseSocket parent = socket.get(Constant.PARENT_SOCKET);
            if (checkCode == MessageDigest.crc32Sum(file)) {
                log.info("数据完整");
                parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收完成"));
            } else {
                log.warn("数据不完整");
                parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收失败"));
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
