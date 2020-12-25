package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.annotation.Enhance;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.aop.SocketReceiveAspect;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ServiceSelector;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.SecurityUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseHandler implements Handler {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected BeanFactory beanFactory = BeanFactory.getInstance();
    private ExecutorService servicePool = Executors.newFixedThreadPool(20);

    static {
        try {
            // 初始化的时候生成RSA密钥对 以后可以把密钥对存起来 这玩意一直变也不是个事
            if (GlobalSettings.privateKey == null || GlobalSettings.publicKey == null) {
                byte[][] keyPair = SecurityUtils.initKey();
                GlobalSettings.publicKey = keyPair[0];
                GlobalSettings.privateKey = keyPair[1];
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void init(ISocket socket) {
        // 创建连接或者接入连接的时候初始化参数
        // 刚开始肯定是没有加密的明文传输
        socket.set(Constant.ENCRYPT, false);
        // 认证标识
        socket.set(Constant.AUTHENTICATED, false);
        // 数字签名算法 加密通信后要对每次发送的消息进行签名
        socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, GlobalSettings.signAlgorithm);
    }

    @Override
    public void onMessage(ISocket socket, Message message) throws BeanException {
        String serviceName = message.get(Constant.SERVICE_NAME);
        execute(socket, serviceName, message);
    }

    @Override
    public void onError(ISocket socket, Throwable throwable) {
        log.error(throwable.getMessage());
    }

    protected void execute(ISocket socket, String serviceName, Message message) throws BeanException {
        String type = socket.get(Constant.TYPE);
        servicePool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    beanFactory.execute(new ServiceSelector(type), serviceName, socket, message);
                } catch (BeanException e) {
                    onError(socket, e);
                }
            }
        });
    }

    public void close() {
        servicePool.shutdown();
    }
}
