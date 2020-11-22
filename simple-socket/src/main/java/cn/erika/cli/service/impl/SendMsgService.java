package cn.erika.cli.service.impl;

import cn.erika.cli.service.ApplicationContext;
import cn.erika.cli.service.CliService;
import cn.erika.socket.handler.impl.ClientHandler;
import cn.erika.socket.handler.impl.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;

public class SendMsgService implements CliService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(String[] args) throws SocketException {
        // TODO 客户端和服务器发送消息的功能必须要分开 不然逻辑太乱了
        // 原本感觉很方便的功能现在看起来跟屎一样
        ServerHandler server = ApplicationContext.get(ServerHandler.class);
        ClientHandler client = ApplicationContext.get(ClientHandler.class);

        StringBuffer message = new StringBuffer();
        if (server != null) {
            String uid = args[1];
            try {
                for (int i = 2; i < args.length; i++) {
                    message.append(args[i]);
                }
                server.send(uid, message.toString());
            } catch (SocketException e) {
                if (client != null) {
                    for (int i = 1; i < args.length; i++) {
                        message.append(args[i]);
                    }
                    client.send(message.toString());
                } else {
                    log.error("未知的uid且客户端未启动");
                }
            }
        } else if (client != null) {
            for (int i = 1; i < args.length; i++) {
                message.append(args[i]);
            }
            client.send(message.toString());
        } else {
            log.error("客户端和服务端均为启动");
        }
    }
}
