package cn.erika.socket.bio.service;

import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Handler;
import cn.erika.socket.common.component.Message;

public interface ISocketService {

    public void client(BaseSocket socket, Message message);

    public void server(BaseSocket socket, Message message);
}
