package cn.erika.cli.services.client;

import cn.erika.cli.services.ICliService;
import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.handler.IClient;

@Component("login")
public class LoginService extends AbstractClientService implements ICliService {
    @Override
    public String info() {
        return "登录认证\n" +
                "\t例如 login admin admin";
    }

    @Override
    public void execute(String... args) throws BeanException {
        super.execute(args);
        IClient client = getBean(IClient.class);
        String username = args[1];
        String password = args[2];

        Message message = new Message();
        message.add(Constant.USERNAME, username);
        message.add(Constant.PASSWORD, password);
        client.execute(Constant.SRV_ACCOUNT_AUTH, message);
    }
}
