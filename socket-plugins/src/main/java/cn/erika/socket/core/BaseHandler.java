package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ServiceSelector;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.DigitalSignature;

import java.net.SocketAddress;

public abstract class BaseHandler implements Handler {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private BeanFactory beanFactory = BeanFactory.getInstance();

    static {
        try {
            // 初始化的时候生成RSA密钥对 以后可以把密钥对存起来 这玩意一直变也不是个事
            if (GlobalSettings.privateKey == null || GlobalSettings.publicKey == null) {
                byte[][] keyPair = DigitalSignature.initKey(GlobalSettings.asymmetricAlgorithm,
                        GlobalSettings.asymmetricKeyLength);
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
        // 创建连接或者接入连接的时候初始化参数
        // 刚开始肯定是没有加密的明文传输
        socket.set(Constant.ENCRYPT, false);
        // 认证标识
        socket.set(Constant.AUTHENTICATED, false);
        // 数字签名算法 加密通信后要对每次发送的消息进行签名
        socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, GlobalSettings.digitalSignatureAlgorithm);
    }

    @Override
    public void onMessage(Socket socket, Message message) throws BeanException {
        String serviceName = message.get(Constant.SERVICE_NAME);
        execute(socket, serviceName, message);
    }

    @Override
    public void onError(Socket socket, Throwable throwable) {
        log.error(throwable.getMessage());
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public abstract SocketAddress getLocalAddress();

    protected void execute(Socket socket, String serviceName, Message message) throws BeanException {
        String type = socket.get(Constant.TYPE);
        beanFactory.execute(new ServiceSelector(type), serviceName, socket, message);
    }
}
