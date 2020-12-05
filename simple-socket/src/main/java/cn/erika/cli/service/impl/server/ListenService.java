package cn.erika.cli.service.impl.server;

import cn.erika.aop.annotation.Component;
import cn.erika.cli.App;
import cn.erika.cli.service.CliService;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.impl.BIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 服务器启动监听端口的方法
 */
@Component("listen")
public class ListenService implements CliService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(String[] args) {
        IServer server = null;
        try {
            if (args.length == 1) {
                server = new BIOServer(GlobalSettings.DEFAULT_ADDRESS, GlobalSettings.DEFAULT_PORT);
            } else {
                String address = args[1];
                int port = Integer.parseInt(args[2]);
                server = new BIOServer(address, port);
            }
            App.addBean(IServer.class, server);
            new Thread(server).start();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
