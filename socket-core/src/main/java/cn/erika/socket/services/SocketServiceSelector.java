package cn.erika.socket.services;

import cn.erika.config.Constant;
import cn.erika.context.bean.BeanSelector;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.Message;

import java.lang.reflect.Method;

/**
 * 自动根据socket的type选择方法
 */
public class SocketServiceSelector implements BeanSelector {
    private String type;

    public SocketServiceSelector(ISocket socket) {
        this.type = socket.get(Constant.TYPE);
    }

    @Override
    public Method getMethod(Class<?> clazz) throws NoSuchMethodException {
        switch (type) {
            case Constant.CLIENT:
                return ISocketService.class.getMethod(Constant.CLIENT, ISocket.class, Message.class);
            case Constant.SERVER:
                return ISocketService.class.getMethod(Constant.SERVER, ISocket.class, Message.class);
            default:
                throw new NoSuchMethodException("找不到指定的方法: " + type);
        }
    }
}
