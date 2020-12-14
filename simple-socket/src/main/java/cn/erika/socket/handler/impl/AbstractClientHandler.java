package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.socket.handler.IClient;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;

public abstract class AbstractClientHandler extends AbstractHandler implements IClient {
    protected InetSocketAddress address;
    protected BaseSocket socket;


    @Override
    public void onOpen(BaseSocket socket) throws BeanException {
        System.out.println("成功连接到服务器");
        App.execute(socket, Constant.SRV_EXCHANGE_KEY, socket, null);
    }

    @Override
    public void onClose(BaseSocket socket) {
        if (socket == null || socket.isClosed()) {
            return;
        }
        System.out.println("正在关闭");
        close(socket);
    }

    @Override
    public void onError(String message, Throwable error) {
        log.error(message, error);
    }

    @Override
    public void close() {
        close(this.socket);
    }

    private void close(BaseSocket socket) {
        if (!socket.isClosed()) {
            Message msg = new Message(Constant.EXIT);
            socket.send(msg);
            socket.close();
        }
    }

    @Override
    public void send(String message) {
        Message msg = new Message(Constant.SRV_TEXT, message);
        socket.send(msg);
    }

    @Override
    public void upload(String filepath, String filename) throws BeanException {
        App.execute(socket, Constant.SRV_PRE_UPLOAD, socket, null);
    }
}
