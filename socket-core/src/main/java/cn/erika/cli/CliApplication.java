package cn.erika.cli;

import cn.erika.cli.services.ICliService;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.exception.BeanException;
import cn.erika.context.exception.NoSuchBeanException;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;
import cn.erika.socket.SocketApplication;
import cn.erika.socket.handler.IClient;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.services.impl.fileTransfer.FileUploadService;
import cn.erika.util.log.ConsoleLogger;
import cn.erika.util.log.FileLogger;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.string.KeyboardReader;
import cn.erika.util.string.StringUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@PackageScan("cn.erika")
public class CliApplication extends SocketApplication implements Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private KeyboardReader reader = KeyboardReader.getInstance();

    public static void main(String[] args) {
        run(CliApplication.class);
    }

    static {
        LoggerFactory.register(new ConsoleLogger());
        LoggerFactory.register(new FileLogger(GlobalSettings.logDir, GlobalSettings.logName, GlobalSettings.charset));
//        LoggerFactory.ignore(FileUploadService.class);
    }

    @Override
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
                if (ICliService.class.isAssignableFrom(clazz)) {
                    Component component = clazz.getAnnotation(Component.class);
                    beanFactory.addBean(component.value(), clazz);
                }
            }
        });
    }

    @Override
    protected void afterStartup() {
        Thread thread = new Thread(this, this.getClass().getSimpleName());
        thread.start();
    }

    @Override
    public void run() {
        log.debug("控制台程序启动");
        try {
            String line;
            while ((line = reader.read()) != null && !Constant.EXIT.equalsIgnoreCase(line)) {
                String[] command = StringUtils.getParam(line);
                try {
                    if (command.length > 0) {
                        ICliService service = beanFactory.getBean(command[0]);
                        service.execute(command);
                    }
                } catch (NumberFormatException e) {
                    log.error("命令错误: " + e.getMessage());
                } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
                    log.error("语法错误: " + line);
                } catch (ClassCastException e) {
                    log.warn("不是控制台服务", e);
                } catch (NoSuchBeanException e) {
                    displayHelp();
                } catch (BeanException e) {
                    log.error(e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            System.exit(1);
        } finally {
            log.info("退出运行");
            try {
                IServer server = beanFactory.getBean(IServer.class);
                if (server != null) {
                    server.close();
                }
            } catch (BeanException e) {
                log.info("服务器未运行");
            }
            try {
                IClient client = beanFactory.getBean(IClient.class);
                if (client != null) {
                    client.close();
                }
            } catch (BeanException e) {
                log.info("客户端未运行");
            }
        }
    }

    private void displayHelp() {
        try {
            Map<String, ICliService> beanList = beanFactory.getBeans(ICliService.class);
            StringBuffer buffer = new StringBuffer("功能说明\n");
            Set<String> names = beanList.keySet();
            LinkedList<String> sortNames = new LinkedList<>();
            sortNames.addAll(names);
            sortNames.sort(new Comparator<String>() {
                @Override
                public int compare(String src, String dest) {
                    return src.compareTo(dest);
                }
            });

            for (String name : sortNames) {
                buffer.append(String.format("%10s", name));
                buffer.append(" : ");
                String info = beanList.get(name).info();
                info = info.replaceAll("\t", "             ");
                buffer.append(info);
                buffer.append("\n");
            }
            buffer.append("\n默认配置在: cn.erika.config.GlobalSettings中");
            System.out.println(buffer);
        } catch (BeanException e) {
            log.error(e.getMessage());
        }
    }
}
