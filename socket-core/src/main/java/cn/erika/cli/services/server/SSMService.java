package cn.erika.cli.services.server;

import cn.erika.cli.exception.ClosedServerException;
import cn.erika.cli.services.CliService;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IServer;

@Component("s_send")
public class SSMService extends BaseService implements CliService {
    @Override
    public String info() {
        return "向客户端发送消息\n" +
                "\t例如 send 2f431f9e-ca3e-4c40-bfd9-f88f63840dcb 你好 这里是服务器\n" +
                "\t第二个参数为连接的UID 通过status命令可以查询";
    }

    @Override
    public void execute(String... args) throws BeanException {
        IServer server = getBean(IServer.class);
        if (server != null && !server.isClosed()) {
            String uid = args[1];
            StringBuffer message = new StringBuffer();
            for (int i = 2; i < args.length; i++) {
                message.append(args[i]).append(" ");
            }
            server.send(uid, message.toString());
        } else {
            throw new ClosedServerException();
        }
    }
}
