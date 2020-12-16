package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.core.DataInfo;
import cn.erika.socket.core.Handler;
import cn.erika.socket.component.Message;
import cn.erika.util.security.RSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractHandler implements Handler {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private ExecutorService servicePool = Executors.newCachedThreadPool();

    public static final Charset CHARSET = GlobalSettings.charset;

    static {
        try {
            byte[][] keyPair = RSA.initKey(GlobalSettings.rsaLength);
            GlobalSettings.publicKey = keyPair[0];
            GlobalSettings.privateKey = keyPair[1];
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void init(BaseSocket socket) {
        socket.set(Constant.ENCRYPT, false);
        socket.set(Constant.AUTHENTICATED, false);
    }

    @Override
    public void onReady(BaseSocket socket) {
        log.info("通讯就绪");
    }

    @Override
    public void onMessage(BaseSocket socket, DataInfo info, Message message) {
        if (!Constant.EXIT.equalsIgnoreCase(message.getHead(Message.Head.ServerName))) {
            String order = message.getHead(Message.Head.ServerName);
            servicePool.submit(new DoIt(socket, order, message));
        } else {
            onClose(socket);
        }
    }

    @Override
    public void onError(String message, Throwable error) {
        log.error(message, error);
    }

    private class DoIt implements Runnable {
        private BaseSocket socket;
        private String order;
        private Message message;

        private DoIt(BaseSocket socket, String order, Message message) {
            this.socket = socket;
            this.order = order;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                App.execute(socket, order, socket, message);
            } catch (BeanException e) {
                onError(e.getMessage(), e);
            }
        }
    }
}
