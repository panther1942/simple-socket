package cn.erika.socket.handler.bio;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.core.component.Task;
import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.io.IOException;

// 因为NIO太麻烦 所以文件传输就弄了BIO
// 实际上文件传输用NIO并没有啥优势
// 文件发送就是个一次性连接 因此用token验证身份即可
// 安全性由FileUploadPreService去做 只要保证父连接的的身份可靠这里就不需要处理
public class FileSender extends BaseHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Message message;
    private ISocket socket;

    public FileSender(ISocket socket, Message message) throws IOException, BeanException {
        this.message = message;
        this.socket = new TcpSocket(socket, this);
    }

    @Override
    public void init(ISocket socket) {
        super.init(socket);
        emptyTasks();
        addTask(new Task() {
            @Override
            public void run() {
                try {
                    execute(socket, Constant.SRV_UPLOAD, message);
                } catch (BeanException e) {
                    onError(socket, e);
                    close();
                }
            }
        });
        try {
            ISocket parent = socket.get(Constant.PARENT_SOCKET);
            String token = message.get(Constant.TOKEN);
            socket.set(Constant.TOKEN, token);
            socket.set(Constant.FILEPATH, parent.get(token));
            parent.remove(token);
            socket.set(Constant.PUBLIC_KEY, parent.get(Constant.PUBLIC_KEY));
            socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, parent.get(Constant.DIGITAL_SIGNATURE_ALGORITHM));
            execute(socket, Constant.SRV_EXCHANGE_TOKEN, null);
        } catch (BeanException e) {
            onError(socket, e);
            close();
        }
    }

    @Override
    public void onConnected(ISocket socket) throws BeanException {
        execute(socket, Constant.SRV_EXCHANGE_KEY, null);
    }

    // 必须要这样写 不然AOP切不进来 Aspectj是静态AOP 编译时插入增强部分
    @Override
    public void onMessage(ISocket socket, Message message) throws BeanException {
        super.onMessage(socket, message);
    }

    @Override
    public void onClose(ISocket socket) {
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
    public void set(String key, Object value) {
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
