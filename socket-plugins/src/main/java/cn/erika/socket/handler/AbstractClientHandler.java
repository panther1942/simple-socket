package cn.erika.socket.handler;

import cn.erika.context.Application;
import cn.erika.socket.config.Constant;
import cn.erika.socket.core.AbstractHandler;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.SocketService;

import java.io.IOException;

public abstract class AbstractClientHandler extends AbstractHandler implements Client {
    protected Socket socket;

    @Override
    public void send(String message) {
        try {
            Message msg = new Message(message);
            socket.send(msg);
        } catch (IOException e) {
            onError(socket, e);
        }
    }

    @Override
    public void upload(String filepath, String filename) {
        SocketService service = Application.getSocketService(Constant.SRV_PRE_UPLOAD);
        service.client(socket, null);
    }

    @Override
    public void disconnect() {
        socket.close();
    }
}
