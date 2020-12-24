package cn.erika.cli.services.impl.client;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.cli.services.CliService;
import cn.erika.socket.handler.IClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端发文本信息的方法
 */
@Component("c_send")
public class CSMService implements CliService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(String[] args) throws BeanException {
        IClient client = App.getBean(IClient.class);
        StringBuffer message = new StringBuffer();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]);
        }
        client.send(message.toString());
    }
}
