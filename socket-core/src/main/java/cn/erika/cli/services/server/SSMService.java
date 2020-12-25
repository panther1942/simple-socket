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
