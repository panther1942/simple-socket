package cn.erika.cli.service.impl.server;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.aop.exception.NoSuchBeanException;
import cn.erika.cli.App;
import cn.erika.cli.service.CliService;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.impl.BIOServer;
import cn.erika.socket.handler.impl.NIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 服务器启动监听端口的方法
 */
@Component("listen")
public class ListenService implements CliService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(String[] args) throws BeanException {
        IServer server = null;
        try {
            try {
                App.getBean(IServer.class);
                log.warn("服务器正在运行");
            } catch (NoSuchBeanException e) {
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
                        server = new BIOServer(address);
                        break;
                    case Constant.NIO:
                        server = new NIOServer(address);
                        break;
                    case Constant.AIO:
//                break;
                    default:
                        throw new BeanException("不支持的Socket类型");
                }
                App.addBean(IServer.class, server);
                new Thread(server).start();
            }
        } catch (IOException e) {
            throw new BeanException(e.getMessage(), e);
        }
    }
}
