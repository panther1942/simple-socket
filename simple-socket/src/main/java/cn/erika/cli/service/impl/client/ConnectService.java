package cn.erika.cli.service.impl.client;

import cn.erika.cli.service.ApplicationContext;
import cn.erika.cli.service.CliService;
import cn.erika.socket.handler.impl.ClientHandler;

public class ConnectService implements CliService {
    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 12345;


    @Override
    public void service(String[] args) {
        ClientHandler client = ApplicationContext.get(ClientHandler.class);
        if (client != null) {
            client.close();
        }
        if (args.length == 1) {
            client = new ClientHandler(DEFAULT_ADDRESS, DEFAULT_PORT);
        }else{
            String address = args[1];
            int port = Integer.parseInt(args[2]);
            client = new ClientHandler(address, port);
        }
        client.connect();
        ApplicationContext.add(ClientHandler.class, client);
    }
}
