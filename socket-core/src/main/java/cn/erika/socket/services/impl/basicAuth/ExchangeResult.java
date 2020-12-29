package cn.erika.socket.services.impl.basicAuth;

import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ISocketService;

@Component(Constant.SRV_EXCHANGE_RESULT)
public class ExchangeResult extends BaseService implements ISocketService {

    @Override
    public void client(ISocket socket, Message message) {
        deal(socket, message);
    }

    @Override
    public void server(ISocket socket, Message message) {
        deal(socket, message);
    }

    private void deal(ISocket socket, Message message) {
        boolean result = message.get(Constant.RESULT);
        String msg = message.get(Constant.TEXT);
        if (result) {
            log.info(msg);
            socket.set(Constant.ENCRYPT, true);
        } else {
            throw new SecurityException(msg);
        }
    }
}
