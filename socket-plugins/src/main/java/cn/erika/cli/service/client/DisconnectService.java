package cn.erika.cli.service.client;

import cn.erika.context.BaseService;
import cn.erika.cli.service.CliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.Client;

@Component("disconnect")
public class DisconnectService extends BaseService implements CliService {
    @Override
    public void execute(String... args) throws BeanException {
        Client client = getBean(Client.class);
        if (client != null) {
            client.disconnect();
        }
    }
}
