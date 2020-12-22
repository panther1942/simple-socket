package cn.erika.socket.services;

import cn.erika.config.Constant;
import cn.erika.context.bean.BeanSelector;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.SocketService;

import java.lang.reflect.Method;

public class ServiceSelector implements BeanSelector {
    private String type;

    public ServiceSelector(String type) {
        this.type = type;
    }

    @Override
    public Method getMethod(Class<?> clazz) throws NoSuchMethodException {
        switch (type) {
            case Constant.CLIENT:
                return SocketService.class.getMethod(Constant.CLIENT, Socket.class, Message.class);
            case Constant.SERVER:
                return SocketService.class.getMethod(Constant.SERVER, Socket.class, Message.class);
            default:
                throw new NoSuchMethodException("找不到指定的方法: " + type);
        }
    }
}
