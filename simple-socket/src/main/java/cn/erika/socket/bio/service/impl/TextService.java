package cn.erika.socket.bio.service.impl;

import cn.erika.socket.bio.core.TcpSocket;
import cn.erika.socket.bio.handler.AbstractHandler;
import cn.erika.socket.bio.handler.Message;
import cn.erika.socket.bio.service.ISocketService;

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
