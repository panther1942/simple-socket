package cn.erika.socket.service.impl;

import cn.erika.config.Constant;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.service.ISocketService;

@SocketServiceMapping(Constant.SRV_TEXT)
public class TextService implements ISocketService {

    @Override
    public void client(BaseSocket socket, Message message) {
        System.out.println("From Server: " + display(message));
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        System.out.println("From Client [" + socket.get(Constant.UID) + "]: " + display(message));
    }

    private String display(Message message) {
        return message.get(Constant.MESSAGE);
    }
}
