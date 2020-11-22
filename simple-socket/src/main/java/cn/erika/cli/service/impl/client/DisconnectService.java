package cn.erika.cli.service.impl.client;

import cn.erika.cli.service.ApplicationContext;
import cn.erika.cli.service.CliService;
import cn.erika.socket.handler.impl.ClientHandler;

public class DisconnectService implements CliService {

    @Override
    public void service(String[] args) {
        ClientHandler client = ApplicationContext.get(ClientHandler.class);
        if (client != null) {
            client.close();
        }
    }
}
