package cn.erika.socket.services.impl.base;

import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.SocketService;

@Component(Constant.SRV_TEXT)
public class TextService extends BaseService implements SocketService {

    @Override
    public void client(Socket socket, Message message) {
        String msg = message.get(Constant.TEXT);
        log.info(String.format("%s : %s", socket.getRemoteAddress(), msg));
    }

    @Override
    public void server(Socket socket, Message message) {
        String msg = message.get(Constant.TEXT);
        log.info(String.format("%s : %s", socket.getRemoteAddress(), msg));
    }
}
