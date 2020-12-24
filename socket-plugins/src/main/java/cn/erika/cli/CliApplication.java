package cn.erika.cli;

import cn.erika.cli.service.CliService;
import cn.erika.config.Constant;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.SocketApplication;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.annotation.Component;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;
import cn.erika.socket.handler.Client;
import cn.erika.socket.handler.Server;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.string.KeyboardReader;
import cn.erika.util.string.StringUtils;

import java.io.IOException;

@PackageScan("cn.erika")
public class CliApplication extends SocketApplication implements Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private KeyboardReader reader = KeyboardReader.getInstance();

    public static void main(String[] args) {
        run(CliApplication.class);
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
                if (CliService.class.isAssignableFrom(clazz)) {
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
        log.debug("Running...");
        try {
            String line;
            while ((line = reader.read()) != null && !Constant.EXIT.equalsIgnoreCase(line)) {
                String[] command = StringUtils.getParam(line);
                try {
                    if (command.length > 0) {
                        CliService service = beanFactory.getBean(command[0]);
                        service.execute(command);
                    }
                } catch (NumberFormatException e) {
                    log.error("命令错误: " + e.getMessage());
                } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
                    log.error("语法错误: " + line);
                } catch (ClassCastException e) {
                    log.warn("不是控制台服务");
                } catch (BeanException e) {
                    log.error(e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            log.info("退出运行");
            try {
                Server server = beanFactory.getBean(Server.class);
                if (server != null) {
                    server.close();
                }
            } catch (BeanException e) {
                log.info("服务器未运行");
            }
            try {
                Client client = beanFactory.getBean(Client.class);
                if (client != null) {
                    client.close();
                }
            } catch (BeanException e) {
                log.info("客户端未运行");
            }
        }
    }
}
