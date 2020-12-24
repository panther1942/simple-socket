package cn.erika.cli.services.impl.client;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.cli.services.CliService;
import cn.erika.socket.handler.IClient;

/**
 * 客户端断开连接的方法
 */
@Component("disconnect")
public class DisconnectService implements CliService {

    @Override
    public void service(String[] args) throws BeanException {
        IClient client = App.getBean(IClient.class);
        client.close();
    }
}
