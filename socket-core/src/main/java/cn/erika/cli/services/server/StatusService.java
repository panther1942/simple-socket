package cn.erika.cli.services.server;

import cn.erika.cli.services.ICliService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IServer;

@Component("status")
public class StatusService extends AbstraceServerService implements ICliService {
    @Override
    public String info() {
        return "查看连接到服务器的客户端信息 能看到UID和远端地址";
    }

    @Override
    public void execute(String... args) throws BeanException {
        super.execute(args);
        IServer server = getBean(IServer.class);
        server.status();
    }
}
