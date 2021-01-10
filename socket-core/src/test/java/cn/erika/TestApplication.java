package cn.erika;

import cn.erika.context.Application;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.context.scan.PackageScanner;
import cn.erika.context.scan.PackageScannerHandler;
import cn.erika.service.IDemoService;
import cn.erika.utils.string.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

@PackageScan({"cn.erika.aop", "cn.erika.service", "cn.erika.socket", "cn.erika.utils"})
public class TestApplication extends Application {
    private BeanFactory beanFactory = BeanFactory.getInstance();

    public static void main(String[] args) {
        run(TestApplication.class);
    }

    @Override
    protected void beforeStartup() {
        super.beforeStartup();
//        try {
//            String mybatisConfig = "mybatis-config.xml";
//            InputStream in = Resources.getResourceAsStream(mybatisConfig);
//            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
//            if (factory != null) {
//                beanFactory.addBean(SqlSessionFactory.class, factory);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        PackageScanner scanner = PackageScanner.getInstance();
        scanner.addHandler(new PackageScannerHandler() {
            @Override
            public boolean filter(Class<?> clazz) {
                return clazz.getAnnotation(Component.class) != null;
            }

            @Override
            public void deal(Class<?> clazz) {
                Component component = clazz.getAnnotation(Component.class);
                if (!StringUtils.isEmpty(component.value())) {
                    beanFactory.addBean(component.value(), clazz);
                }
            }
        });
    }

    @Override
    protected void afterStartup() {
        try {
//            System.out.println(beanFactory.execute("sum", 1, 2));
            IDemoService demoService = beanFactory.getBean("demoService");
            demoService.say();

            /*SqlSessionFactory factory = beanFactory.getBean(SqlSessionFactory.class);
            try(SqlSession session = factory.openSession()){
                AccountDao dao = session.getMapper(AccountDao.class);
                List<Account> list = dao.getAccountList();
                for (Account account : list) {
                    System.out.println(account);
                }
            }*/
        } catch (BeanException e) {
            e.printStackTrace();
        }

    }
}
