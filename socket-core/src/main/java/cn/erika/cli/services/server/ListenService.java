package cn.erika.cli.services.server;

import cn.erika.cli.services.ICliService;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.bio.BIOServer;
import cn.erika.socket.handler.nio.NIOServer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Component("listen")
public class ListenService extends BaseService implements ICliService {

    @Override
    public String info() {
        return "开始监听指定的地址\n" +
                "\t例如: listen localhost 12345\n" +
                "\t如果不指定地址 将监听localhost:43037";
    }

    @Override
    public void execute(String... args) throws BeanException {
        IServer server = getBean(IServer.class);
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
            host = GlobalSettings.DEFAULT_ADDRESS;
            port = GlobalSettings.DEFAULT_PORT;
        }
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            switch (GlobalSettings.type) {
                case Constant.AIO:
                    log.warn("暂未实现");
                    return;
                case Constant.NIO:
                    server = new NIOServer(address);
                    break;
                case Constant.BIO:
                    server = new BIOServer(address);
                    break;
                default:
                    throw new BeanException("不支持的模式: " + GlobalSettings.type);
            }

            addBean(IServer.class, server);
            server.listen();
        } catch (BindException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            throw new BeanException(e);
        }
    }
}
