package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.core.TcpSocket;

import java.io.IOException;
import java.net.SocketAddress;

public class FileSender extends AbstractHandler {
    private TcpSocket socket;

    public FileSender(BaseSocket socket, String sessionToken) throws IOException {
        SocketAddress address = socket.getSocket().getRemoteSocketAddress();
        this.socket = new TcpSocket(address, this, GlobalSettings.charset);
        this.socket.set(Constant.SESSION_TOKEN, sessionToken);
        this.socket.set(Constant.PUBLIC_KEY, socket.get(Constant.PUBLIC_KEY));
    }

    @Override
    public void onOpen(BaseSocket socket) throws BeanException {
        socket.set(Constant.TYPE, Constant.CLIENT);
        App.execute(socket, Constant.SRV_EXCHANGE_TOKEN, socket, null);
    }

    @Override
    public void onReady(BaseSocket socket) {
        try {
            App.execute(socket, Constant.SRV_UPLOAD, socket, null);
        } catch (BeanException e) {
            onError(e.getMessage(), e);
        }
    }

    @Override
    public void onClose(BaseSocket socket) {

    }

    @Override
    public void onError(String message, Throwable error) {

    }
}
