package cn.erika.cli.service.impl.client;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.aop.exception.NoSuchBeanException;
import cn.erika.cli.App;
import cn.erika.cli.service.CliService;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.handler.IClient;
import cn.erika.socket.handler.impl.BIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端连接服务器的方法
 */
@Component("connect")
public class ConnectService implements CliService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(String[] args) throws BeanException {
        IClient client = null;
        try {
            client = App.getBean(IClient.class);
            if (client != null) {
                client.close();
            }
        } catch (NoSuchBeanException e) {
            logger.debug("客户端未启动，尝试启动客户端");
        }
        if (args.length == 1) {
            client = new BIOClient(GlobalSettings.DEFAULT_ADDRESS, GlobalSettings.DEFAULT_PORT);
        } else {
            String address = args[1];
            int port = Integer.parseInt(args[2]);
            client = new BIOClient(address, port);
        }
        client.connect();
        App.addBean(IClient.class, client);
    }
}
