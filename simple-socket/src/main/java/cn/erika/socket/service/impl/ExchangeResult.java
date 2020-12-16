package cn.erika.socket.service.impl;

import cn.erika.config.Constant;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.service.ISocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SocketServiceMapping(Constant.SRV_EXCHANGE_RESULT)
public class ExchangeResult implements ISocketService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(BaseSocket socket, Message message) {
        boolean result = message.get(Constant.RESULT);
        String msg = message.get(Constant.MESSAGE);
        if (result) {
            log.info(msg);
            socket.set(Constant.ENCRYPT, true);
        } else {
            log.warn(msg);
//            socket.set(Constant.ENCRYPT, false);
        }
    }

    @Override
    public void server(BaseSocket socket, Message message) {
    }
}
