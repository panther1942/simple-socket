package cn.erika.web.configuration;

import cn.erika.web.service.IDemoService;
import cn.erika.web.service.impl.Demo2ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class DemoCommandLineRunner implements CommandLineRunner {
    @Autowired
    private IDemoService demoService;

    @Autowired
    private Demo2ServiceImpl demo2Service;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("运行");
        System.out.println(demoService.sum(1, 2));

        demo2Service.say();
    }
}
