package cn.erika.cli.services.server;

import cn.erika.cli.exception.ClosedServerException;
import cn.erika.cli.services.CliService;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IServer;

@Component("status")
public class StatusService extends BaseService implements CliService {
    @Override
    public void execute(String... args) throws BeanException {
        IServer server = getBean(IServer.class);
        if (server != null && !server.isClosed()) {
            server.status();
        } else {
            throw new ClosedServerException();
        }
    }
}
