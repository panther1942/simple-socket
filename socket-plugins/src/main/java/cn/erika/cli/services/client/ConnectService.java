package cn.erika.cli.services.client;

import cn.erika.context.BaseService;
import cn.erika.cli.services.CliService;
import cn.erika.config.GlobalSettings;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.BIOClient;
import cn.erika.socket.handler.Client;

import java.io.IOException;
import java.net.InetSocketAddress;

@Component("connect")
public class ConnectService extends BaseService implements CliService {
    @Override
    public void execute(String... args) throws BeanException {
        Client client = getBean(Client.class);
        if (client != null && !client.isClosed()) {
            client.close();
        }

        String host;
        int port;

        if (args.length >= 3) {
            host = args[1];
            port = Integer.parseInt(args[2]);
        } else {
            host = GlobalSettings.DEFAULT_ADDRESS;
            port = GlobalSettings.DEFAULT_PORT;
        }
        try {
            client = new BIOClient(new InetSocketAddress(host, port));
            addBean(Client.class, client);
            client.connect();
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }
}
