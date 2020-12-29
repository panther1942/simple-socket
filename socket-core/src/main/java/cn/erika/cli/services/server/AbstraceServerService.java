package cn.erika.cli.services.server;

import cn.erika.cli.exception.ClosedServerException;
import cn.erika.cli.services.ICliService;
import cn.erika.context.BaseService;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IServer;

public abstract class AbstraceServerService extends BaseService implements ICliService {

    @Override
    public void execute(String... args) throws BeanException {
        IServer server = getBean(IServer.class);
        if (server == null || server.isClosed()) {
            throw new ClosedServerException();
        }
    }
}
