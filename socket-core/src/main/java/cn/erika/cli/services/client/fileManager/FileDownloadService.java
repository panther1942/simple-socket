package cn.erika.cli.services.client.fileManager;

import cn.erika.cli.services.ICliService;
import cn.erika.cli.services.client.basic.BaseClientService;
import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IClient;
import cn.erika.socket.model.pto.Message;

@Component("download")
public class FileDownloadService extends BaseClientService implements ICliService {
    @Override
    public String info() {
        return "从服务器下载文件\n" +
                "\t例如 download /var/log/message.log message_20201225.log\n";
    }

    @Override
    public void execute(String... args) throws BeanException {
        super.execute(args);
        IClient client = getBean(IClient.class);
        String filepath = args[1];
        String filename = args[2];
        Message message = new Message();
        message.add(Constant.REMOTE_FILE, filepath);
        message.add(Constant.LOCAL_FILE, filename);
        client.execute(Constant.SRV_PRE_DOWNLOAD, message);
    }
}
