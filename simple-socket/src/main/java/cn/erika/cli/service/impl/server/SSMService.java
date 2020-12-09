package cn.erika.cli.service.impl.server;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.cli.service.CliService;
import cn.erika.socket.handler.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 服务器向客户端发送文本消息的方法
 */
@Component("s_send")
public class SSMService implements CliService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(String[] args) throws BeanException {
        IServer server = App.getBean(IServer.class);
        StringBuffer message = new StringBuffer();
        if (server != null) {
            String uid = args[1];
            for (int i = 2; i < args.length; i++) {
                message.append(args[i]);
            }
            server.send(uid, message.toString());
        }
    }
}
