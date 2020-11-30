package cn.erika.cli;

import cn.erika.cli.service.ApplicationContext;
import cn.erika.cli.service.CliService;
import cn.erika.cli.service.impl.SendMsgService;
import cn.erika.cli.service.impl.client.ConnectService;
import cn.erika.cli.service.impl.client.DisconnectService;
import cn.erika.cli.service.impl.server.DisplayClientListService;
import cn.erika.cli.service.impl.server.ListenService;
import cn.erika.cli.util.KeyboardReader;
import cn.erika.socket.Constant;
import cn.erika.socket.handler.impl.ClientHandler;
import cn.erika.socket.handler.impl.ServerHandler;
import cn.erika.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;

public class App implements Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ServerHandler server;
    private ClientHandler client;
    private KeyboardReader reader = KeyboardReader.getInstance();

    public static void main(String[] args) {
        App app = new App();
        app.init();
        new Thread(app).start();
    }

    private void init() {
        try {
            ApplicationContext.register(ConnectService.class);
            ApplicationContext.register(DisconnectService.class);
            ApplicationContext.register(DisplayClientListService.class);
            ApplicationContext.register(ListenService.class);
            ApplicationContext.register(SendMsgService.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }


        ApplicationContext.register("connect", ConnectService.class);
        ApplicationContext.register("conn", ConnectService.class);
        ApplicationContext.register("c", ConnectService.class);
        ApplicationContext.register("disconnect", DisconnectService.class);
        ApplicationContext.register("disconn", DisconnectService.class);
        ApplicationContext.register("d", DisconnectService.class);
        ApplicationContext.register("listen", ListenService.class);
        ApplicationContext.register("l", ListenService.class);
        ApplicationContext.register("display", DisplayClientListService.class);
        ApplicationContext.register("send", SendMsgService.class);
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
                        CliService service = ApplicationContext.getService(command[0]);
                        if (service != null) {
                            service.service(command);
                        }
                    }
                } catch (NullPointerException e) {
                    log.error("不支持的命令: " + command[0]);
                } catch (NumberFormatException e) {
                    log.error("命令错误: " + e.getMessage());
                } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
                    log.error("语法错误: " + line);
                } catch (SocketException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } finally {
            log.info("退出运行");
            if (client != null) {
                client.close();
            }
            if (server != null) {
                server.close();
            }
        }
    }
}
