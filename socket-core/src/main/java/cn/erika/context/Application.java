package cn.erika.context;

import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 全局容器 不要多次生成该类对象 因为BeanFactory是单例的
 */
public abstract class Application {
    private static Logger log = LoggerFactory.getLogger(Application.class);

    // bean工厂 用于存储需要缓存的bean
    protected BeanFactory beanFactory = BeanFactory.getInstance();

    /**
     * 用于启动容器
     * 设置为静态方法是为了支持启动类不是Application的子类
     *
     * @param clazz 启动类必须是Application的子类 且具有可访问的无参构造函数
     */
    public static void run(Class<? extends Application> clazz) {
        try {
            Thread thread = Thread.currentThread();
            thread.setName("main");
            printSysInfo();
            Application app = clazz.newInstance();
            try {
                app.beforeStartup();
                app.scanPackage(clazz);
                app.afterStartup();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("启动类无法实例化", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("启动类需要一个可访问的无参构造函数", e);
        }
    }

    private static void printSysInfo() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        log.info("Starting Application on " + osName + " [" + osVersion + " " + osArch + "]");
    }

    /**
     * 扫包之前注册扫包处理器 以处理扫到的类
     */
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
                    Component component = method.getAnnotation(Component.class);
                    if (component != null) {
                        beanFactory.addBean(component.value(), method);
                    }
                }
            }
        });
    }

    /**
     * 扫描之后执行的动作
     */
    protected abstract void afterStartup();

    /**
     * 扫包 允许在PackageScan上添加多个包路径
     *
     * @param clazz 将获取该类上的PackageScan注解 确定要扫的包
     * @throws IOException 如果在扫包过程中出现错误则抛出该异常
     */
    private void scanPackage(Class<?> clazz) throws IOException {
        PackageScan scan = clazz.getAnnotation(PackageScan.class);
        if (scan != null) {
            PackageScanner scanner = PackageScanner.getInstance();
            for (String pack : scan.value()) {
                scanner.addPackage(pack);
            }
            scanner.scan();
        }
    }

    public static void scanPackage() throws IOException {
        PackageScanner scanner = PackageScanner.getInstance();
        scanner.scan();
    }

}
