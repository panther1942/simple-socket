package cn.erika.socket.plugins;

import cn.erika.context.annotation.Component;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.DataInfo;

@Component
public class CompressPlugin implements SocketPlugin {
    @Override
    public void afterReady(Socket socket) {
        socket.set("compress", "GZIP");
    }

    @Override
    public DataInfo beforeReceive(Socket socket, DataInfo dataInfo) {
        return null;
    }

    @Override
    public DataInfo beforeSend(Socket socket, DataInfo dataInfo) {
        return null;
    }
}
