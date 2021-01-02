package cn.erika.cli.services.server;

import cn.erika.cli.services.ICliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IServer;

@Component("abort")
public class AbortListenService extends BasicServerService implements ICliService {
    @Override
    public String info() {
        return "中断服务器监听并释放所有与客户端的连接";
    }

    @Override
    public void execute(String... args) throws BeanException {
        super.execute(args);
        IServer server = getBean(IServer.class);
        server.close();
    }
}
