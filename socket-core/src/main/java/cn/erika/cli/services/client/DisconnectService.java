package cn.erika.cli.services.client;

import cn.erika.cli.services.ICliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IClient;

@Component("disconnect")
public class DisconnectService extends BasicClientService implements ICliService {
    @Override
    public String info() {
        return "断开与服务器的连接";
    }

    @Override
    public void execute(String... args) throws BeanException {
        super.execute(args);
        IClient client = getBean(IClient.class);
        client.close();
    }
}
