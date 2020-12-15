package cn.erika.socket.service.impl;

import cn.erika.aop.annotation.Component;
import cn.erika.socket.service.ISocketService;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.component.Message;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.util.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(Constant.SRV_EXCHANGE_KEY)
public class ExchangeKey implements ISocketService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(BaseSocket socket, Message message) {
        // 向客户端请求公钥的时候不需要请求体 message只需要放上order就能获取客户端的公钥
        Message request = new Message(Constant.SRV_EXCHANGE_KEY);
        request.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
        socket.send(request);
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        try {
            byte[] clientPublicKey = message.get(Constant.PUBLIC_KEY);
            if (clientPublicKey == null) {
                throw new SecurityException("缺少公钥信息");
            }
            socket.set(Constant.PUBLIC_KEY, clientPublicKey);
            Message reply = new Message(Constant.SRV_EXCHANGE_PASSWORD);
            reply.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
            socket.send(reply);
            log.debug("对端请求加密通信");
        } catch (SecurityException e) {
            log.error(e.getMessage(), e);
        }


    }
}
