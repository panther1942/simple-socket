package cn.erika.socket.plugins;

import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.DataInfo;

public interface SocketPlugin {
    public void afterReady(Socket socket);

    public DataInfo beforeReceive(Socket socket, DataInfo dataInfo);

    public DataInfo beforeSend(Socket socket, DataInfo dataInfo);
}
