package cn.erika.cli.services.client;

import cn.erika.cli.exception.ClosedClientException;
import cn.erika.context.BaseService;
import cn.erika.cli.services.CliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.Client;

@Component("disconnect")
public class DisconnectService extends BaseService implements CliService {
    @Override
    public void execute(String... args) throws BeanException {
        Client client = getBean(Client.class);
        if (client != null && !client.isClosed()) {
            client.close();
        }else{
            throw new ClosedClientException();
        }
    }
}
