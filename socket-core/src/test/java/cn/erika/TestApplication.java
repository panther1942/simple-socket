package cn.erika;

import cn.erika.context.Application;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.jdbc.dao.AccountDao;
import cn.erika.jdbc.model.Account;
import cn.erika.utils.db.JdbcUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@PackageScan("cn.erika")
public class TestApplication extends Application {
    private BeanFactory beanFactory = BeanFactory.getInstance();

    public static void main(String[] args) {
        run(TestApplication.class);
    }

    @Override
    protected void beforeStartup() {
        super.beforeStartup();
        try {
            String mybatisConfig = "mybatis-config.xml";
            InputStream in = Resources.getResourceAsStream(mybatisConfig);
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
            if (factory != null) {
                beanFactory.addBean(SqlSessionFactory.class, factory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void afterStartup() {
        try {
            System.out.println(beanFactory.execute("sum", 1, 2));
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
