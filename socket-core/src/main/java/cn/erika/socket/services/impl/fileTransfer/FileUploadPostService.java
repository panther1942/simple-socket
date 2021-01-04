package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.services.ISocketService;

@Component(Constant.SRV_POST_UPLOAD)
public class FileUploadPostService extends BaseService implements ISocketService {

    @Override
    public void client(ISocket socket, Message message) {
        String msg = message.get(Constant.TEXT);
        log.info(msg);
    }

    @Override
    public void server(ISocket socket, Message message) {
        socket.send(new Message(Constant.SRV_EXIT));
        socket.close();
    }
}
