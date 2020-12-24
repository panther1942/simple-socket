package cn.erika.cli.services.server;

import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.cli.services.CliService;
import cn.erika.config.GlobalSettings;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.BIOServer;
import cn.erika.socket.handler.NIOServer;
import cn.erika.socket.handler.Server;

import java.io.IOException;
import java.net.InetSocketAddress;

@Component("listen")
public class ListenService extends BaseService implements CliService {

    @Override
    public void execute(String... args) throws BeanException {
        Server server = getBean(Server.class);
        if (server != null && !server.isClosed()) {
            log.info("服务器正在运行: " + server.getLocalAddress());
            return;
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
            switch (GlobalSettings.type) {
                case Constant.AIO:
                    log.warn("暂未实现");
                    return;
                case Constant.NIO:
                    server = new NIOServer(new InetSocketAddress(host, port));
                    break;
                case Constant.BIO:
                    server = new BIOServer(new InetSocketAddress(host, port));
                    break;
                default:
                    throw new BeanException("不支持的模式: " + GlobalSettings.type);
            }
            addBean(Server.class, server);
            server.listen();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
