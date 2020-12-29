package cn.erika.socket;

import cn.erika.context.Application;
import cn.erika.context.annotation.Component;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;
import cn.erika.socket.services.ISocketService;

/**
 * 适配Socket通信的Application的直接子类
 * 注册了Socket相关服务的扫描处理器
 */
public abstract class SocketApplication extends Application {

    protected void beforeStartup() {
        super.beforeStartup();
        PackageScanner scanner = PackageScanner.getInstance();
        scanner.addHandler(new PackageScannerHandler() {
            @Override
            public boolean filter(Class<?> clazz) {
                return clazz.getAnnotation(Component.class) != null;
            }

            @Override
            public void deal(Class<?> clazz) {
                if (ISocketService.class.isAssignableFrom(clazz)) {
                    Component component = clazz.getAnnotation(Component.class);
                    beanFactory.addBean(component.value(), clazz);
                }
            }
        });
    }
}
