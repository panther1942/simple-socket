package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.DataInfo;
import cn.erika.socket.common.component.Handler;
import cn.erika.socket.common.component.Message;
import cn.erika.socket.common.exception.TokenException;
import cn.erika.util.security.RSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractHandler implements Handler {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public static final Charset CHARSET = GlobalSettings.charset;

    static {
        try {
            byte[][] keyPair = RSA.initKey(GlobalSettings.rsaLength);
            GlobalSettings.publicKey = keyPair[0];
            GlobalSettings.privateKey = keyPair[1];
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
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
        try {
            if (!Constant.EXIT.equalsIgnoreCase(message.getHead(Message.Head.Order))) {
                String order = message.getHead(Message.Head.Order);
                App.execute(socket, order, socket, message);
            } else {
                onClose(socket);
            }
        } catch (BeanException e) {
            //未知的服务请求
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void onError(String message, Throwable error) {
        log.error(message, error);
    }
}
