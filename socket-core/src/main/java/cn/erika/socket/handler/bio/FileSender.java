package cn.erika.socket.handler.bio;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.core.Task;
import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.socket.handler.BaseClient;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.io.IOException;

// 因为NIO太麻烦 所以文件传输就弄了BIO
// 实际上文件传输用NIO并没有啥优势
// 文件发送就是个一次性连接 因此用token验证身份即可
// 安全性由FileUploadPreService去做 只要保证父连接的的身份可靠这里就不需要处理
public class FileSender extends BaseClient {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String localFile;
    private String remoteFile;
    private FileInfo fileInfo;

    public FileSender(ISocket socket, String localFile, String remoteFile, FileInfo fileInfo) throws IOException {
        super();
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.fileInfo = fileInfo;
        new TcpSocket(socket, this);
    }

    @Override
    public void init(ISocket socket) {
        super.init(socket);
        emptyTasks();
        // 添加发送文件的任务 在身份验证完成后执行
        addTask(new Task() {
            @Override
            public void run() {
                try {
                    Message message = new Message();
                    message.add(Constant.LOCAL_FILE, localFile);
                    message.add(Constant.REMOTE_FILE, remoteFile);
                    message.add(Constant.FILE_INFO, fileInfo);
                    execute(socket, Constant.SRV_UPLOAD, message);
                } catch (BeanException e) {
                    onError(socket, e);
                    close();
                }
            }
        });

        try {
            // 初始化参数
            ISocket parent = socket.get(Constant.PARENT_SOCKET);
            String token = fileInfo.getPartToken();
            socket.set(Constant.TOKEN, token);
            socket.set(Constant.PUBLIC_KEY, parent.get(Constant.PUBLIC_KEY));
            socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, parent.get(Constant.DIGITAL_SIGNATURE_ALGORITHM));
            // 执行连接后动作(身份校验-令牌)
            onConnected(socket);
        } catch (BeanException e) {
            onError(socket, e);
            close();
        }
    }

    @Override
    public void onConnected(ISocket socket) throws BeanException {
        execute(socket, Constant.SRV_EXCHANGE_TOKEN, null);
    }

    @Override
    public void onClose(ISocket socket) {
        log.info("关闭传输管道");
    }
}
