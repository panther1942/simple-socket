package cn.erika.cli.services.client;

import cn.erika.cli.services.ICliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IClient;

@Component("c_send")
public class CSMService extends AbstractClientService implements ICliService {
    @Override
    public String info() {
        return "向服务器发送消息\n" +
                "\t例如 c_send 你好 我是客户端";
    }

    @Override
    public void execute(String... args) throws BeanException {
        super.execute(args);
        IClient client = getBean(IClient.class);
        StringBuffer message = new StringBuffer();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }
        client.send(message.toString());
    }
}
