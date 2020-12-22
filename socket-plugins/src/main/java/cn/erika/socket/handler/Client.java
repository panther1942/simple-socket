package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

public abstract class Client extends BaseHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    protected Socket socket;

    @Override
    public void init(Socket socket) {
        super.init(socket);
        System.out.println("连接到服务器");
    }

    @Override
    public void onClose(Socket socket) {
        log.info("从服务器断开连接");
    }

    public void execute(String serviceName, Message message) throws BeanException {
        execute(socket, serviceName, message);
    }

    public abstract void connect();

    public abstract void disconnect();

    public void send(String message) {
        socket.send(new Message(Constant.SRV_TEXT, message));
    }
}
