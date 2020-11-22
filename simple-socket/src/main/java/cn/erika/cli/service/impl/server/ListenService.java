package cn.erika.cli.service.impl.server;

import cn.erika.cli.service.CliService;
import cn.erika.socket.handler.impl.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ListenService implements CliService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 12345;

    @Override
    public void service(String[] args) {
        ServerHandler server = null;
        try {
            if (args.length == 1) {
                server = new ServerHandler(DEFAULT_ADDRESS, DEFAULT_PORT);
            } else {
                String address = args[1];
                int port = Integer.parseInt(args[2]);
                server = new ServerHandler(address, port);
            }
            new Thread(server).start();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
