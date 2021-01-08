package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.core.Task;

import java.net.SocketAddress;
import java.util.List;

public abstract class BaseClient extends BaseHandler {
    protected ISocket socket;

    public BaseClient() {
        List<Task> taskList = beanFactory.getTasks(Constant.CLIENT);
        addTasks(taskList);
    }

    @Override
    public void init(ISocket socket) {
        super.init(socket);
        log.info("连接到服务器: " + socket.getRemoteAddress());
    }

    @Override
    public void onConnected(ISocket socket) throws BeanException {
        execute(socket, Constant.SRV_EXCHANGE_KEY, null);
    }

    @Override
    public void onClose(ISocket socket) {
        log.info("从服务器断开连接");
    }

    public void execute(String serviceName, Message message) throws BeanException {
        execute(socket, serviceName, message);
    }

    public void send(String message) {
        if (socket != null) {
            socket.send(new Message(Constant.SRV_TEXT, message));
        } else {
            log.warn("未连接到服务器");
        }
    }

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

    @Override
    public void add(String key, Object value) {
        socket.set(key, value);
    }

    @Override
    public <T> T get(String key) {
        return socket.get(key);
    }

    @Override
    public void remove(String key) {
        socket.remove(key);
    }
}
