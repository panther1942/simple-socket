package cn.erika.cli.services.client.fileManager;

import cn.erika.cli.services.ICliService;
import cn.erika.cli.services.client.basic.BaseClientService;
import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.handler.IClient;

@Component("upload")
public class FileUploadService extends BaseClientService implements ICliService {
    @Override
    public String info() {
        return "上传文件到服务器\n" +
                "\t例如 upload /var/log/message.log message_20201225.log\n" +
                "\t将上传文件至服务器程序工作目录的downloads目录下";
    }

    @Override
    public void execute(String... args) throws BeanException {
        super.execute(args);
        IClient client = getBean(IClient.class);
        String filepath = args[1];
        String filename = args[2];
        Message message = new Message();
        message.add(Constant.FILEPATH, filepath);
        message.add(Constant.FILENAME, filename);
        client.execute(Constant.SRV_PRE_UPLOAD, message);
    }
}
