package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.Application;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.FileInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ISocketService;
import cn.erika.util.security.MessageDigest;

import java.io.File;
import java.io.IOException;

@Component(Constant.SRV_POST_UPLOAD)
public class FileUploadPostService extends BaseService implements ISocketService {
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    public void client(ISocket socket, Message message) {
        String msg = message.get(Constant.TEXT);
        log.info(msg);
    }

    @Override
    public void server(ISocket socket, Message message) {
        socket.close();
    }
}
