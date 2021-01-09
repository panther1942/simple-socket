package cn.erika.socket.handler.bio;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.Task;
import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.socket.handler.BaseClient;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.io.IOException;

public class FileReceiver extends BaseClient {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String localFile;
    private String remoteFile;
    private FileInfo fileInfo;
    private FileInfo partInfo;
    private String fileToken;

    public FileReceiver(ISocket socket, String localFile, String remoteFile, FileInfo fileInfo, FileInfo partInfo, String fileToken) throws IOException {
        super();
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.fileInfo = fileInfo;
        this.partInfo = partInfo;
        this.fileToken = fileToken;
        new TcpSocket(socket, this);
    }

    @Override
    public void init(ISocket socket) {
        super.init(socket);
        emptyTasks();
        addTask(new Task() {
            @Override
            public void run() {
                try {
                    Message message = new Message();
                    message.add(Constant.REMOTE_FILE, remoteFile);
                    socket.set(Constant.LOCAL_FILE, localFile);
                    socket.set(Constant.FILE_INFO, fileInfo);
                    socket.set(Constant.FILE_PART_INFO, partInfo);
                    socket.set(Constant.TOKEN, fileToken);
                    execute(socket, Constant.SRV_DOWNLOAD, message);
                } catch (BeanException e) {
                    onError(socket, e);
                    close();
                }
            }
        });

        try {
            ISocket parent = socket.get(Constant.PARENT_SOCKET);
            String token = partInfo.getPartToken();
            socket.set(fileToken, parent.get(fileToken));
            socket.set(Constant.TOKEN, token);
            socket.set(Constant.PUBLIC_KEY, parent.get(Constant.PUBLIC_KEY));
            socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, parent.get(Constant.DIGITAL_SIGNATURE_ALGORITHM));
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
