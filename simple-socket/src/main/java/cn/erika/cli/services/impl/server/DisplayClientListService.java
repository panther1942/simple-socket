package cn.erika.cli.services.impl.server;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.aop.exception.NoSuchBeanException;
import cn.erika.cli.App;
import cn.erika.cli.services.CliService;
import cn.erika.socket.handler.IServer;

/**
 * 服务器显示接入连接的方法
 */
@Component("display")
public class DisplayClientListService implements CliService {

    @Override
    public void service(String[] args) throws BeanException {
        try {
            IServer server = App.getBean(IServer.class);
            server.displayLink();
        } catch (NoSuchBeanException e) {
            throw new BeanException("服务器未启动");
        }
    }
}
