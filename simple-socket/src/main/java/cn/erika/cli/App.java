package cn.erika.cli;

import cn.erika.aop.annotation.PackageScan;
import cn.erika.aop.component.SocketApplication;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.service.CliService;
import cn.erika.config.Constant;
import cn.erika.socket.handler.IClient;
import cn.erika.socket.handler.IServer;
import cn.erika.util.string.KeyboardReader;
import cn.erika.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PackageScan("cn.erika")
public class App extends SocketApplication implements Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private KeyboardReader reader = KeyboardReader.getInstance();

    public static void main(String[] args) {
        App.run(App.class);
    }

    @Override
    public void beforeStartup() {
        super.beforeStartup();
        excludeBean(IClient.class);
        excludeBean(IServer.class);
    }

    @Override
    public void afterStartup() {
        new Thread(this, this.getClass().getSimpleName()).start();
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
                        CliService service = App.getBean(command[0]);
                        service.service(command);
                    }
                } catch (NumberFormatException e) {
                    log.error("命令错误: " + e.getMessage());
                } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
                    log.error("语法错误: " + line);
                } catch (BeanException e) {
                    log.error(e.getMessage());
                }
            }
        } finally {
            log.info("退出运行");
            try {
                IServer server = getBean(IServer.class);
                if (server != null) {
                    server.close();
                }
            } catch (BeanException e) {
                log.info("服务器未运行");
            }
            try {
                IClient client = getBean(IClient.class);
                if (client != null) {
                    client.close();
                }
            } catch (BeanException e) {
                log.info("客户端未运行");
            }
        }
    }
}
