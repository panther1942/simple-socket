package cn.erika.socket.bio.service.impl;

import cn.erika.aop.annotation.Component;
import cn.erika.socket.bio.service.ISocketService;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.config.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(Constant.SRV_EXCHANGE_RESULT)
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
            socket.set(Constant.ENCRYPT, false);
            socket.close();
        }
    }

    @Override
    public void server(BaseSocket socket, Message message) {
    }
}
