package cn.erika.socket;

import cn.erika.context.Application;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;
import cn.erika.socket.handler.Client;
import cn.erika.socket.handler.Server;
import cn.erika.socket.plugins.SocketPlugin;
import cn.erika.socket.services.SocketService;

import java.util.LinkedList;
import java.util.List;

public abstract class SocketApplication extends Application {
    private static List<SocketPlugin> socketPlugins = new LinkedList<>();

    protected void beforeStartup() {
        beanFactory.excludeBean(Server.class);
        beanFactory.excludeBean(Client.class);
        super.beforeStartup();
        PackageScanner scanner = PackageScanner.getInstance();
        scanner.addHandler(new PackageScannerHandler() {
            @Override
            public boolean filter(Class<?> clazz) {
                return clazz.getAnnotation(Component.class) != null;
            }

            @Override
            public void deal(Class<?> clazz) {
                try {
                    if (SocketPlugin.class.isAssignableFrom(clazz)) {
                        socketPlugins.add(beanFactory.createBean(clazz));
                    }
                    if (SocketService.class.isAssignableFrom(clazz)) {
                        Component component = clazz.getAnnotation(Component.class);
                        beanFactory.addBean(component.value(), clazz);
                    }
                } catch (BeanException e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    public static List<SocketPlugin> getSocketPlugins() {
        return socketPlugins;
    }
}
