package cn.erika.socket.services.impl.auth;

import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ISocketService;

@Component(Constant.SRV_ACCOUNT_AUTH)
public class AccountAuth extends BaseService implements ISocketService {
    @Override
    public void client(ISocket socket, Message message) {
        if (message != null) {
            if (message.get(Constant.SERVICE_NAME) == null) {
                String username = message.get(Constant.USERNAME);
                String password = message.get(Constant.PASSWORD);
                Message request = new Message(Constant.SRV_ACCOUNT_AUTH);
                request.add(Constant.USERNAME, username);
                request.add(Constant.PASSWORD, password);
                socket.send(request);
            } else {
                boolean result = message.get(Constant.RESULT);
                if (result) {
                    log.info("认证成功");
                    socket.set(Constant.AUTHENTICATED, true);
                } else {
                    log.warn("认证失败");
                }
            }
        }
    }

    @Override
    public void server(ISocket socket, Message message) {
        Message reply = new Message(Constant.SRV_ACCOUNT_AUTH);
        String username = message.get(Constant.USERNAME);
        String password = message.get(Constant.PASSWORD);
        if ("admin".equals(username) && "admin".equals(password)) {
            socket.set(Constant.AUTHENTICATED, true);
            reply.add(Constant.RESULT, true);
        } else {
            reply.add(Constant.RESULT, false);
        }
        socket.send(reply);
    }
}
