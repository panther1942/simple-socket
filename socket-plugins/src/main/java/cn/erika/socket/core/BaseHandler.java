package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ServiceSelector;
import cn.erika.util.security.RSA;

import java.net.SocketAddress;

public abstract class BaseHandler implements Handler {
    private BeanFactory beanFactory = BeanFactory.getInstance();

    static {
        try {
            if (GlobalSettings.privateKey == null || GlobalSettings.publicKey == null) {
                byte[][] keyPair = RSA.initKey(GlobalSettings.rsaLength);
                GlobalSettings.publicKey = keyPair[0];
                GlobalSettings.privateKey = keyPair[1];
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void init(Socket socket) {
        socket.set(Constant.ENCRYPT, false);
        socket.set(Constant.AUTHENTICATED, false);
        socket.set(Constant.RSA_SIGN_ALGORITHM, GlobalSettings.rsaDigestAlgorithm);
    }

    @Override
    public void onMessage(Socket socket, Message message) throws BeanException {
        String serviceName = message.get(Constant.SERVICE_NAME);
        execute(socket, serviceName, message);
    }

    @Override
    public void onError(Socket socket, Throwable throwable) {
        System.err.println(throwable.getMessage());
        socket.close();
    }

    public abstract SocketAddress getLocalAddress();

    protected void execute(Socket socket, String serviceName, Message message) throws BeanException {
        String type = socket.get(Constant.TYPE);
        beanFactory.execute(new ServiceSelector(type), serviceName, socket, message);
    }
}
