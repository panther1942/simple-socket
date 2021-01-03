package cn.erika.socket.handler.bio;

import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.socket.handler.BaseClient;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.io.IOException;

public class FileReceiver extends BaseClient {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Message message;

    public FileReceiver(ISocket socket, Message message) throws IOException, BeanException {
        super();
        this.message = message;
        new TcpSocket(socket, this);
    }

}
