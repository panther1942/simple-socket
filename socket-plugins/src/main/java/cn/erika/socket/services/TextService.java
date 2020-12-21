package cn.erika.socket.services;

import cn.erika.context.annotation.Component;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;

@Component("srv_text")
public class TextService implements SocketService {

    @Override
    public void client(Socket socket, Message message) {

    }

    @Override
    public void server(Socket socket, Message message) {

    }
}
