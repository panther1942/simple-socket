package cn.erika.socket;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.Application;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.exception.BeanException;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.bio.BIOServer;
import cn.erika.socket.handler.nio.NIOServer;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.log.ConsoleLogger;
import cn.erika.utils.log.FileLogger;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.string.StringUtils;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * 适配Socket通信的Application的直接子类
 * 注册了Socket相关服务的扫描处理器
 */
@PackageScan("cn.erika")
public class SocketApplication extends Application {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        run(SocketApplication.class);
    }

    static {
        LoggerFactory.register(new ConsoleLogger());
        LoggerFactory.register(new FileLogger(GlobalSettings.logDir, GlobalSettings.logName, GlobalSettings.charset));
    }

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
                Component component = clazz.getAnnotation(Component.class);
                if (!StringUtils.isEmpty(component.value())) {
                    beanFactory.addBean(component.value(), clazz);
                }
            }
        });
    }

    @Override
    protected void afterStartup() {
        log.info("尝试启动服务器...");
        String host = GlobalSettings.DEFAULT_ADDRESS;
        int port = GlobalSettings.DEFAULT_PORT;
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            IServer server = null;
            switch (GlobalSettings.type) {
                case Constant.AIO:
                    log.warn("暂未实现");
                    return;
                case Constant.NIO:
                    server = new NIOServer(address);
                    break;
                case Constant.BIO:
                    server = new BIOServer(address);
                    break;
                default:
                    throw new BeanException("不支持的模式: " + GlobalSettings.type);
            }
            beanFactory.addBean(IServer.class, server);
            server.listen();
            log.info("服务器开始运行");
        } catch (BindException e) {
            log.error(e.getMessage());
        } catch (IOException | BeanException e) {
            log.error("服务启动失败", e);
        }
    }
}
