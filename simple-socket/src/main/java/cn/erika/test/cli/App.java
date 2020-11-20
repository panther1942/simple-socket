package cn.erika.test.cli;

import cn.erika.test.cli.service.CliService;
import cn.erika.test.cli.util.KeyboardReader;
import cn.erika.test.socket.handler.StringDefine;
import cn.erika.test.socket.handler.impl.ClientHandler;
import cn.erika.test.socket.handler.impl.ServerHandler;
import cn.erika.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class App implements Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 12345;

    private ServerHandler server;
    private ClientHandler client;
    private KeyboardReader reader = KeyboardReader.getInstance();

    private Map<String, CliService> serviceList = new HashMap<>();

    public static void main(String[] args) {
        App app = new App();
        app.init();
        new Thread(app).start();
    }

    private void init() {
        CliService connService = params -> {
            if (params.length == 1) {
                client = new ClientHandler(DEFAULT_ADDRESS, DEFAULT_PORT);
            } else {
                String address = params[1];
                int port = Integer.parseInt(params[2]);
                client = new ClientHandler(address, port);
            }
            client.connect();
        };
        addService("connect", connService);
        addService("conn", connService);
        addService("c", connService);
        addService("disconn", params -> {
            client.close();
        });
        addService("list", params -> {
            if (server != null) {
                server.displayLink();
            } else {
                log.error("服务器未启动");
            }
        });
        CliService listenService = params -> {
            try {
                if (params.length == 1) {
                    server = new ServerHandler(DEFAULT_ADDRESS, DEFAULT_PORT);
                } else {
                    String address = params[1];
                    int port = Integer.parseInt(params[2]);
                    server = new ServerHandler(address, port);
                }
                new Thread(server).start();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        };
        addService("listen", listenService);
        addService("l", listenService);
        addService("send", params -> {
            StringBuffer message = new StringBuffer();
            if (server != null) {
                String uid = params[1];
                try {
                    for (int i = 2; i < params.length; i++) {
                        message.append(params[i]);
                    }
                    server.send(uid, message.toString());
                } catch (SocketException e) {
                    for (int i = 1; i < params.length; i++) {
                        message.append(params[i]);
                    }
                    client.send(message.toString());
                }
            } else if (client != null) {
                for (int i = 1; i < params.length; i++) {
                    message.append(params[i]);
                }
                client.send(message.toString());
            } else {
                log.error("客户端和服务端均为启动");
            }
        });
    }

    private void addService(String serviceName, CliService service) {
        serviceList.put(serviceName, service);
    }


    @Override
    public void run() {
        log.debug("Running...");
        try {
            String line;
            while ((line = reader.read()) != null && !StringDefine.EXIT.equalsIgnoreCase(line)) {
                String[] command = StringUtils.getParam(line);
                try {
                    if (command.length > 0) {
                        serviceList.get(command[0]).service(command);
                    }
                } catch (NullPointerException e) {
                    log.error("不支持的命令: " + command[0]);
                } catch (NumberFormatException e) {
                    log.error("命令错误: " + e.getMessage());
                } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
                    log.error("语法错误: " + line);
                }
            }
        } finally {
            log.info("退出运行");
            if (client != null) {
                client.close();
            }
            if (server != null) {
                server.close();
            }
        }
    }
}
