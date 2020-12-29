package cn.erika;

import cn.erika.context.Application;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;

@PackageScan("cn.erika")
public class TestApplication extends Application {
    private BeanFactory beanFactory = BeanFactory.getInstance();

    public static void main(String[] args) {
        run(TestApplication.class);
    }

    @Override
    protected void afterStartup() {
        try {
            System.out.println(beanFactory.execute("sum", 1, 2));
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }
}
