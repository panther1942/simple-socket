package cn.erika.test.socket.service.impl;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.Message;
import cn.erika.test.socket.service.ISocketService;

public class TextService implements ISocketService {
    @Override
    public void client(AbstractHandler handler, TcpSocket socket, Message message) {
        System.out.println("From Server: " + display(message));
    }

    @Override
    public void server(AbstractHandler handler, TcpSocket socket, Message message) {
        System.out.println("From Client [" + socket.get("id") + "]: " + display(message));
    }

    private String display(Message message) {
        return new String(message.getPayload(), AbstractHandler.CHARSET);
    }
}
