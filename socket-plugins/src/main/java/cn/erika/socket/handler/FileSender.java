package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;

// 因为NIO太麻烦 所以文件传输就弄了BIO
// 实际上文件传输用NIO并没有啥优势
// 文件发送就是个一次性连接 因此用token验证身份即可
// 安全性由FileUploadPreService去做 只要保证父连接的的身份可靠这里就不需要处理
public class FileSender extends BaseHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Message message;
    private Socket socket;

    public FileSender(Socket socket, Message message) throws IOException {
        this.message = message;
        SocketAddress address = socket.getRemoteAddress();
        this.socket = new TcpSocket(address, this);
        this.socket.set(Constant.PARENT_SOCKET, socket);
    }

    @Override
    public void init(Socket socket) {
        try {
            super.init(socket);
            String token = message.get(Constant.TOKEN);
            socket.set(Constant.TOKEN, token);
            socket.set(Constant.PUBLIC_KEY, socket.get(Constant.PUBLIC_KEY));
            socket.set(Constant.RSA_SIGN_ALGORITHM, socket.get(Constant.RSA_SIGN_ALGORITHM));
            execute(socket, Constant.SRV_EXCHANGE_TOKEN, null);
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Socket socket, Message message) throws BeanException {
        super.onMessage(socket, message);
    }

    public void upload(){
        try {
            execute(socket, Constant.SRV_UPLOAD, message);
        } catch (BeanException e) {
            onError(socket, e);
        }
    }

    @Override
    public void onClose(Socket socket) {
        log.info("关闭传输管道");
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public SocketAddress getLocalAddress() {
        try {
            return socket.getLocalAddress();
        } catch (IOException e) {
            log.error("无法获取本地地址");
        }
        return null;
    }
}
