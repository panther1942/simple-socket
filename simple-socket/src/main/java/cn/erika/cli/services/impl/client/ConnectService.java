package cn.erika.cli.services.impl.client;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.aop.exception.NoSuchBeanException;
import cn.erika.cli.App;
import cn.erika.cli.services.CliService;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.handler.IClient;
import cn.erika.socket.handler.impl.BIOClient;
import cn.erika.socket.handler.impl.NIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

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
        InetSocketAddress address = null;
        if (args.length == 1) {
            address = new InetSocketAddress(GlobalSettings.DEFAULT_ADDRESS, GlobalSettings.DEFAULT_PORT);
        } else {
            String host = args[1];
            int port = Integer.parseInt(args[2]);
            address = new InetSocketAddress(host, port);
        }
        switch (GlobalSettings.type) {
            case Constant.BIO:
                client = new BIOClient(address);
                break;
            case Constant.NIO:
                client = new NIOClient(address);
                break;
            case Constant.AIO:
//                break;
            default:
                throw new BeanException("不支持的Socket类型");
        }
        client.connect();
        App.addBean(IClient.class, client);
    }
}
