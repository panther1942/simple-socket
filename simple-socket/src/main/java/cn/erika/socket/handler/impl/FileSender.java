package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.TcpSocket;

import java.io.IOException;
import java.net.SocketAddress;

// 因为NIO太麻烦 所以文件传输就弄了BIO
// 实际上文件传输用NIO并没有啥优势
// 文件发送就是个一次性连接 因此用token验证身份即可
// 安全性由FileUploadPreService去做 只要保证父连接的的身份可靠这里就不需要处理
public class FileSender extends AbstractHandler {
    private Message message;

    public FileSender(BaseSocket socket, Message message) throws IOException {
        SocketAddress address = socket.getRemoteAddress();
        this.message = message;
        TcpSocket fileSocket = new TcpSocket(address, this, GlobalSettings.charset);
        fileSocket.set(Constant.PARENT_SOCKET, socket);
    }

    @Override
    public void onOpen(BaseSocket socket) throws BeanException {
        String sessionToken = message.get(Constant.SESSION_TOKEN);
        socket.set(Constant.SESSION_TOKEN, sessionToken);
        socket.set(Constant.PUBLIC_KEY, socket.get(Constant.PUBLIC_KEY));
        socket.set(Constant.RSA_ALGORITHM, socket.get(Constant.RSA_ALGORITHM));
        App.execute(socket, Constant.SRV_EXCHANGE_TOKEN, socket, null);
    }

    @Override
    public void onReady(BaseSocket socket) {
        try {
            App.execute(socket, Constant.SRV_UPLOAD, socket, message);
        } catch (BeanException e) {
            onError(e.getMessage(), e);
        }
    }

    @Override
    public void onClose(BaseSocket socket) {
        socket.close();
    }

    @Override
    public void onError(String message, Throwable error) {
        log.error(message, error);
    }
}
