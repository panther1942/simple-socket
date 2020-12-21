package cn.erika.socket.core;

import cn.erika.context.Application;
import cn.erika.socket.config.Constant;
import cn.erika.socket.config.GlobalSettings;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.ServiceException;
import cn.erika.socket.services.SocketService;
import cn.erika.util.security.RSA;

import java.security.NoSuchAlgorithmException;

public abstract class AbstractHandler implements Handler {
    static {
        try {
            if (GlobalSettings.privateKey == null || GlobalSettings.publicKey == null) {
                byte[][] keyPair = RSA.initKey(GlobalSettings.rsaLength);
                GlobalSettings.publicKey = keyPair[0];
                GlobalSettings.privateKey = keyPair[1];
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void init(Socket socket) {
        socket.set(Constant.ENCRYPT, false);
        socket.set(Constant.AUTHENTICATED, false);
    }

    @Override
    public void onMessage(Socket socket, Message message) throws ServiceException {
        String serverName = message.get(Constant.SERVICE_NAME);
        SocketService service = Application.getSocketService(serverName);
        String type = message.get(Constant.TYPE);
        switch (type) {
            case Constant.CLIENT:
                service.client(socket, message);
                break;
            case Constant.SERVER:
                service.server(socket, message);
                break;
            default:
                throw new ServiceException("无法识别服务类型: " + type);
        }
    }

    @Override
    public void onError(Socket socket, Throwable throwable) {
        System.err.println(throwable.getMessage());
        socket.close();
    }


}
