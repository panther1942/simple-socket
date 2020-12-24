package cn.erika.cli.service.client;

import cn.erika.context.BaseService;
import cn.erika.cli.service.CliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.Client;

@Component("c_send")
public class CSMService extends BaseService implements CliService {
    @Override
    public void execute(String... args) throws BeanException {
        Client client = getBean(Client.class);
        if (client != null && !client.isClosed()) {
            StringBuffer message = new StringBuffer();
            for (int i = 1; i < args.length; i++) {
                message.append(args[i]);
            }
            client.send(message.toString());
        } else {
            throw new BeanException("客户端没有运行");
        }
    }
}
