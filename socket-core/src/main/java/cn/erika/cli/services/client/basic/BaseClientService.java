package cn.erika.cli.services.client.basic;

import cn.erika.cli.exception.ClosedClientException;
import cn.erika.context.BaseService;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IClient;

public abstract class BaseClientService extends BaseService {

    public void execute(String... args) throws BeanException {
        IClient client = getBean(IClient.class);
        if (client == null || client.isClosed()) {
            throw new ClosedClientException();
        }
    }
}
