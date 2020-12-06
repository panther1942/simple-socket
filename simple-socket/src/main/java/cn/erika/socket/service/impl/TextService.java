package cn.erika.socket.service.impl;

import cn.erika.aop.annotation.Component;
import cn.erika.socket.service.ISocketService;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.config.Constant;

@Component(Constant.SRV_TEXT)
public class TextService implements ISocketService {

    @Override
    public void client(BaseSocket socket, Message message) {
        System.out.println("From Server: " + display(message));
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        System.out.println("From Client [" + socket.get("id") + "]: " + display(message));
    }

    private String display(Message message) {
        return message.get(Constant.MESSAGE);
    }
}
