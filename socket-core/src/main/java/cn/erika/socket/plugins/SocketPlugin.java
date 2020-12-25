package cn.erika.socket.plugins;

import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.DataInfo;

public interface SocketPlugin {
    public void afterReady(ISocket socket);

    public DataInfo afterReceive(ISocket socket, DataInfo dataInfo);

    public DataInfo beforeSend(ISocket socket, DataInfo dataInfo);
}
