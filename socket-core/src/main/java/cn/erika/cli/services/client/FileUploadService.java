package cn.erika.cli.services.client;

import cn.erika.cli.exception.ClosedClientException;
import cn.erika.cli.services.CliService;
import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.handler.IClient;

@Component("upload")
public class FileUploadService extends BaseService implements CliService {
    @Override
    public void execute(String... args) throws BeanException {
        IClient client = getBean(IClient.class);
        if (client != null && !client.isClosed()) {
            String filepath = args[1];
            String filename = args[2];
            Message message = new Message();
            message.add(Constant.FILEPATH, filepath);
            message.add(Constant.FILENAME, filename);
            client.execute(Constant.SRV_PRE_UPLOAD, message);
        } else {
            throw new ClosedClientException();
        }
    }
}
