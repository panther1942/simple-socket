package cn.erika.test.cli.service.impl.server;

import cn.erika.test.cli.service.ApplicationContext;
import cn.erika.test.cli.service.CliService;
import cn.erika.test.socket.handler.impl.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayClientListService implements CliService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(String[] args) {
        ServerHandler server = ApplicationContext.get(ServerHandler.class);
        if (server != null) {
            server.displayLink();
        } else {
            log.error("服务器未启动");
        }
    }
}
