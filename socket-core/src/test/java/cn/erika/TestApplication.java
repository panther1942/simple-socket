package cn.erika;

import cn.erika.context.Application;
import cn.erika.context.annotation.PackageScan;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.service.DemoServiceImpl;
import cn.erika.service.IDemoService;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.MessageDigest;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

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
