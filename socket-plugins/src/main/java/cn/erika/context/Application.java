package cn.erika.context;

import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;
import cn.erika.socket.plugins.SocketPlugin;
import cn.erika.socket.services.SocketService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Application {
    private static List<SocketPlugin> socketPlugins = new LinkedList<>();
    private static Map<String, SocketService> services = new HashMap<>();
    private static Map<String, Object> settings = new ConcurrentHashMap<>();
    private static Map<String, Object> storage = new ConcurrentHashMap<>();

    private BeanFactory beanFactory = BeanFactory.getInstance();

    public static void run(Class<? extends Application> clazz, String... args) {
        try {
            Application app = clazz.newInstance();
            app.run(args);
        } catch (InstantiationException e) {
            throw new RuntimeException("启动类无法实例化", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("启动类需要一个可访问的无参构造函数", e);
        }
    }

    private void run(String... args) {
        Class<? extends Application> clazz = this.getClass();
        beforeStartup();
        scanPackage(clazz);
        afterStartup();
    }

    protected void beforeStartup() {
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
                        SocketPlugin plugin = (SocketPlugin) clazz.newInstance();
                        socketPlugins.add(plugin);
                    }
                    if (SocketService.class.isAssignableFrom(clazz)) {
                        Component component = clazz.getAnnotation(Component.class);
                        SocketService plugin = (SocketService) clazz.newInstance();
                        services.put(component.value(), plugin);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void scanPackage(Class<? extends Application> clazz) {
        PackageScan scan = clazz.getAnnotation(PackageScan.class);
        if (scan != null) {
            PackageScanner scanner = PackageScanner.getInstance();
            for (String pack : scan.value()) {
                scanner.addPackage(pack);
            }
            scanner.scan();
        }
    }

    protected abstract void afterStartup();

    public static List<SocketPlugin> getSocketPlugins() {
        return socketPlugins;
    }

    public static SocketService getSocketService(String serviceName) {
        return services.get(serviceName);
    }
}
