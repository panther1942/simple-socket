package cn.erika.test.cli.service.impl;

import cn.erika.test.cli.service.ApplicationContext;
import cn.erika.test.cli.service.CliService;
import cn.erika.test.socket.handler.impl.ClientHandler;

public class ConnectServiceImpl implements CliService {

    static{
        ApplicationContext.register(ConnectServiceImpl.class);
    }

    @Override
    public void service(String[] args) {
        ClientHandler client = ApplicationContext.get(ClientHandler.class);
        if (client != null) {
            client.close();
        }
        String address = args[1];
        int port = Integer.parseInt(args[2]);
        client = new ClientHandler(address, port);
        client.connect();
        ApplicationContext.add(ClientHandler.class, client);
    }
}
