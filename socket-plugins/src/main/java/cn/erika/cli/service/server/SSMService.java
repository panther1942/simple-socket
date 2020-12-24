package cn.erika.cli.service.server;

import cn.erika.context.BaseService;
import cn.erika.cli.service.CliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.Server;

@Component("s_send")
public class SSMService extends BaseService implements CliService {
    @Override
    public void execute(String... args) throws BeanException {
        Server server = getBean(Server.class);
        if (server != null && !server.isClosed()) {
            String uid = args[1];
            StringBuffer message = new StringBuffer();
            for (int i = 2; i < args.length; i++) {
                message.append(args[i]).append(" ");
            }
            server.send(uid, message.toString());
        } else {
            throw new BeanException("服务器未启动");
        }
    }
}
