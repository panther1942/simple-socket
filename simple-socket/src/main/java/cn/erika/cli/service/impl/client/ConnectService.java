package cn.erika.cli.service.impl.client;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.cli.service.CliService;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.handler.IClient;
import cn.erika.socket.handler.impl.BIOClient;

@Component("connect")
public class ConnectService implements CliService {

    @Override
    public void service(String[] args) throws BeanException {
        IClient client = App.getBean(IClient.class);
        if (client != null) {
            client.close();
        }
        if (args.length == 1) {
            client = new BIOClient(GlobalSettings.DEFAULT_ADDRESS, GlobalSettings.DEFAULT_PORT);
        } else {
            String address = args[1];
            int port = Integer.parseInt(args[2]);
            client = new BIOClient(address, port);
        }
        client.connect();
        App.addBean(IClient.class, client);
    }
}
