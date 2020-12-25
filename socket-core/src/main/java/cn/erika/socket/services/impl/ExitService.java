package cn.erika.socket.services.impl;

import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.SocketService;

@Component(Constant.SRV_EXIT)
public class ExitService extends BaseService implements SocketService {
    @Override
    public void client(ISocket socket, Message message) {
        socket.close();
    }

    @Override
    public void server(ISocket socket, Message message) {
        socket.close();
    }
}