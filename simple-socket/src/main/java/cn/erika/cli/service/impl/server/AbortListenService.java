package cn.erika.cli.service.impl.server;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.cli.service.CliService;
import cn.erika.socket.handler.IServer;

@Component("quit")
public class AbortListenService implements CliService {
    @Override
    public void service(String[] args) throws BeanException {
        IServer server = App.getBean(IServer.class);
        server.close();
    }
}