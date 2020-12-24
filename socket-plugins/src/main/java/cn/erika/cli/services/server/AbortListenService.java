package cn.erika.cli.services.server;

import cn.erika.cli.exception.ClosedServerException;
import cn.erika.context.BaseService;
import cn.erika.cli.services.CliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.Server;

@Component("abort")
public class AbortListenService extends BaseService implements CliService {
    @Override
    public void execute(String... args) throws BeanException {
        Server server = getBean(Server.class);
        if (server != null && !server.isClosed()) {
            server.close();
        } else {
            throw new ClosedServerException();
        }
    }
}
