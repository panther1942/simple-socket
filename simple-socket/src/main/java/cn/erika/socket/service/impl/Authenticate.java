package cn.erika.socket.service.impl;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.component.Message;
import cn.erika.socket.service.ISocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SocketServiceMapping(Constant.SRV_LOGIN)
public class Authenticate implements ISocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(BaseSocket socket, Message message) {
        if (message == null) {
            Message request = new Message(Constant.SRV_LOGIN);
            request.add(Constant.USERNAME, GlobalSettings.username);
            request.add(Constant.PASSWORD, GlobalSettings.password);
            socket.send(request);
        } else {
            boolean flag = message.get(Constant.RESULT);
            String msg = message.get(Constant.MESSAGE);
            socket.set(Constant.AUTHENTICATED, flag);
            log.info(msg);
        }
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        String username = message.get(Constant.USERNAME);
        String password = message.get(Constant.PASSWORD);
        if ("admin".equals(username) && "admin".equals(password)) {
            socket.set(Constant.AUTHENTICATED, true);

            Message reply = new Message(Constant.SRV_LOGIN);
            reply.add(Constant.RESULT, true);
            reply.add(Constant.MESSAGE, "登录成功");
            socket.send(reply);
        } else {
            Message reply = new Message(Constant.SRV_LOGIN);
            reply.add(Constant.RESULT, false);
            reply.add(Constant.MESSAGE, "登录失败");
            socket.send(reply);
        }
    }
}
