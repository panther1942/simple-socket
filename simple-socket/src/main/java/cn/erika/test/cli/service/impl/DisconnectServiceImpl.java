package cn.erika.test.cli.service.impl;

import cn.erika.test.cli.service.ApplicationContext;
import cn.erika.test.cli.service.CliService;
import cn.erika.test.socket.handler.impl.ClientHandler;

public class DisconnectServiceImpl implements CliService {
    static {
        ApplicationContext.register(DisconnectServiceImpl.class);
    }

    @Override
    public void service(String[] args) {
        ClientHandler client = ApplicationContext.get(ClientHandler.class);
        if (client != null) {
            client.close();
        }
    }
}
