package cn.erika.context;

import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.annotation.ServiceMapping;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Application {
    private static Map<String, Object> storage = new ConcurrentHashMap<>();
    protected BeanFactory beanFactory = BeanFactory.getInstance();

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

    protected void run(String... args) {
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
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    ServiceMapping mapping = method.getAnnotation(ServiceMapping.class);
                    if (mapping != null) {
                        beanFactory.addBean(mapping.value(), method);
                    }
                }
            }
        });
    }

    protected abstract void afterStartup();

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


    public static void set(String key, Object value) {
        storage.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) storage.get(key);
    }

    public static void remove(String key) {
        storage.remove(key);
    }

}
