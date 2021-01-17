package cn.erika;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.SocketApplication;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.core.Task;
import cn.erika.utils.security.algorithm.BasicSecurityAlgorithm;

@PackageScan({"cn.erika.aop", "cn.erika.config", "cn.erika.socket", "cn.erika.utils", "cn.erika.service"})
public class TestSocketApplication extends SocketApplication {

    public static void main(String[] args) {
        run(TestSocketApplication.class);
    }

    @Override
    protected void beforeStartup() {
        super.beforeStartup();
        try {
            beanFactory.addTasks(Constant.SERVER, new Task() {
                @Override
                public void run() {
                    socket.send(new Message(Constant.SRV_TEXT, "你好 这里是服务器"));
                }
            });
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void afterStartup() {
        super.afterStartup();
    }
}
