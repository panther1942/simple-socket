package cn.erika.cli.service.server;

import cn.erika.context.BaseService;
import cn.erika.cli.service.CliService;
import cn.erika.config.GlobalSettings;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.BIOServer;
import cn.erika.socket.handler.Server;

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
            host = GlobalSettings.DEFUALT_ADDRESS;
            port = GlobalSettings.DEFAULT_PORT;
        }
        server = new BIOServer(new InetSocketAddress(host, port));
        addBean(Server.class, server);
        server.listen();
        log.info("服务器监听端口: " + server.getLocalAddress());
    }
}
