package cn.erika.aop.component;

import cn.erika.aop.exception.BeanException;
import cn.erika.aop.scan.PackageScanner;
import cn.erika.aop.scan.PackageScannerHandler;
import cn.erika.config.Constant;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.service.ISocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public abstract class SocketApplication extends Application {
    private static Map<String, Class<? extends ISocketService>> socketServiceList = new HashMap<>();
    private static Logger log = LoggerFactory.getLogger(SocketApplication.class);

    @Override
    public void beforeStartup() {
        PackageScanner scanner = PackageScanner.getInstance();
        scanner.addHandler(new PackageScannerHandler() {
            @Override
            public boolean filter(Class<?> clazz) {
                return ISocketService.class.isAssignableFrom(clazz) && clazz.getAnnotation(SocketServiceMapping.class) != null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void deal(Class<?> clazz) {
                SocketServiceMapping component = clazz.getAnnotation(SocketServiceMapping.class);
                Class<ISocketService> service = (Class<ISocketService>) clazz;
                if (component != null) {
                    socketServiceList.put(component.value(), service);
                }
            }
        });
    }

    public static Object execute(BaseSocket socket, String name, Object... args) throws BeanException {
        Class<? extends ISocketService> clazz = socketServiceList.get(name);
        Object object = getBean(clazz);
        Method method = null;
        String type = socket.get(Constant.TYPE);
        try {
            switch (type) {
                case Constant.CLIENT:
                    method = clazz.getMethod(Constant.CLIENT, BaseSocket.class, Message.class);
                    break;
                case Constant.SERVER:
                    method = clazz.getMethod(Constant.SERVER, BaseSocket.class, Message.class);
                    break;
                default:
                    throw new NoSuchMethodException("无此方法: " + name);
            }
            try {
                return Proxy.getInvocationHandler(object).invoke(object, method, args);
            } catch (Throwable throwable) {
                throw new BeanException("内部异常", throwable);
            }
        } catch (NoSuchMethodException e) {
            throw new BeanException(e.getMessage(), e);
        }
    }
}
