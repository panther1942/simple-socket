package cn.erika.cli.services.client.fileManager;

import cn.erika.cli.services.ICliService;
import cn.erika.cli.services.client.basic.BaseClientService;
import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.handler.IClient;

@Component("ls")
public class ListFilesService extends BaseClientService implements ICliService {
    @Override
    public String info() {
        return "查询服务器指定路径上的文件 如果不指定路径则查询工作目录";
    }

    @Override
    public void execute(String... args) throws BeanException {
        super.execute(args);
        IClient client = getBean(IClient.class);
        Message data = new Message();
        if (args.length > 1) {
            String target = args[1];
            data.add(Constant.FILEPATH, target);
        }
        client.execute(Constant.SRV_LS, data);
    }
}
