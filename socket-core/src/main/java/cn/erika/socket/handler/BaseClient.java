package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;

import java.net.SocketAddress;

public abstract class BaseClient extends BaseHandler implements IClient{
    protected ISocket socket;

    @Override
    public void init(ISocket socket) {
        super.init(socket);
        log.info("连接到服务器: " + socket.getRemoteAddress());
    }

    @Override
    public void onClose(ISocket socket) {
        log.info("从服务器断开连接");
    }

    @Override
    public void execute(String serviceName, Message message) throws BeanException {
        execute(socket, serviceName, message);
    }

    @Override
    public void send(String message) {
        socket.send(new Message(Constant.SRV_TEXT, message));
    }

    @Override
    public SocketAddress getLocalAddress() {
        if (socket != null) {
            return socket.getLocalAddress();
        }
        return null;
    }

    @Override
    public void close() {
        super.close();
        if (socket != null && !socket.isClosed()) {
            socket.send(new Message(Constant.SRV_EXIT, Constant.EXIT));
            if (!socket.isClosed()) {
                socket.close();
            }
        }
    }

    @Override
    public boolean isClosed() {
        return socket != null && socket.isClosed();
    }
}