package cn.erika.cli.service.impl.client;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.aop.exception.NoSuchBeanException;
import cn.erika.cli.App;
import cn.erika.cli.service.CliService;
import cn.erika.config.Constant;
import cn.erika.socket.handler.IClient;

@Component("upload")
public class FileUploadService implements CliService {
    @Override
    public void service(String[] args) throws BeanException {
        try {
            IClient client = App.getBean(IClient.class);

            String filepath = args[1];
            String filename = args[2];

            App.add(Constant.FILENAME, filename);
            App.add(Constant.FILEPATH, filepath);
            client.upload(filepath, filename);
        } catch (NoSuchBeanException e) {
            throw new BeanException("客户端未启动");
        }
    }
}
