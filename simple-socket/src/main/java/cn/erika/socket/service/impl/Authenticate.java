package cn.erika.socket.service.impl;

import cn.erika.aop.annotation.Component;
import cn.erika.socket.service.ISocketService;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

@Component(Constant.SRV_LOGIN)
public class Authenticate implements ISocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(BaseSocket socket, Message message) {
        if (message == null) {
            socket.send(new Message(Constant.SRV_LOGIN, new HashMap<String, Object>() {
                {
                    put(Constant.USERNAME, GlobalSettings.username);
                    put(Constant.PASSWORD, GlobalSettings.password);
                }
            }));
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
            socket.send(new Message(Constant.SRV_LOGIN, new HashMap<String, Object>() {
                {
                    put(Constant.RESULT, true);
                    put(Constant.MESSAGE, "登录成功");
                }
            }));
            socket.set(Constant.AUTHENTICATED, true);
        } else {
            socket.send(new Message(Constant.SRV_LOGIN, new HashMap<String, Object>() {
                {
                    put(Constant.RESULT, false);
                    put(Constant.MESSAGE, "登录失败");
                }
            }));
        }
    }
}
