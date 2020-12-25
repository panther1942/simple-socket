package cn.erika.cli.services.client;

import cn.erika.cli.services.CliService;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IClient;
import cn.erika.socket.handler.bio.BIOClient;
import cn.erika.socket.handler.nio.NIOClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Component("connect")
public class ConnectService extends BaseService implements CliService {
    @Override
    public String info() {
        return "连接到服务器\n" +
                "\t例如 connect localhost 12345\n" +
                "\t如果当前已经连接到服务器 将断开之前的连接\n" +
                "\t如果不指定地址 将连接localhost:43037";
    }

    @Override
    public void execute(String... args) throws BeanException {
        IClient client = getBean(IClient.class);
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
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            switch (GlobalSettings.type) {
                case Constant.AIO:
                    log.warn("暂未实现");
                    return;
                case Constant.NIO:
                    client = createBean(NIOClient.class, address);
                    break;
                case Constant.BIO:
                    client = createBean(BIOClient.class, address);
                    break;
                default:
                    throw new BeanException("不支持的模式: " + GlobalSettings.type);
            }

            addBean(IClient.class, client);
            client.connect();
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }
}
