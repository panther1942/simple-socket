package cn.erika;

import cn.erika.context.Application;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.service.DemoServiceImpl;
import cn.erika.service.IDemoService;
import org.junit.Test;

@PackageScan("cn.erika")
public class TestApplication extends Application {
    private BeanFactory beanFactory = BeanFactory.getInstance();

    public static void main(String[] args) {
        run(TestApplication.class, args);
    }

    @Override
    protected void afterStartup() {
        try {
            System.out.println(beanFactory.execute("sum", 1, 2));
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInterface() {
        IDemoService demoService = new DemoServiceImpl();
        demoService.say();
        System.out.println(demoService.sum(1, 1));
        System.out.println("end");
    }
}
