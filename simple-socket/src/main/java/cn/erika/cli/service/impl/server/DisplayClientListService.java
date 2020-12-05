package cn.erika.cli.service.impl.server;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.cli.service.CliService;
import cn.erika.socket.handler.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器显示接入连接的方法
 */
@Component("display")
public class DisplayClientListService implements CliService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(String[] args) throws BeanException {
        IServer server = App.getBean(IServer.class);
        if (server != null) {
            server.displayLink();
        } else {
            log.error("服务器未启动");
        }
    }
}
