package cn.erika;

import cn.erika.context.Application;
import cn.erika.context.annotation.PackageScan;

@PackageScan("cn.erika")
public class SocketApplication extends Application {

    public static void main(String[] args) {
        Application.run(SocketApplication.class, args);
    }

    @Override
    protected void afterStartup() {
//        TcpSocket socket = new TcpSocket();
//        socket.ready();
        /*socket.send(new HashMap<String, Object>() {
            {
                put("hello", "world");
            }
        });*/
    }
}
