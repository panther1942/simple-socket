package cn.erika.web.configuration;

import cn.erika.web.service.IDemoService;
import cn.erika.web.service.impl.Demo2ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class DemoCommandLineRunner implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IDemoService demoService;

    @Autowired
    private Demo2ServiceImpl demo2Service;

    @Override
    public void run(String... args) throws Exception {
        log.debug("程序启动");
        System.out.println("运行");
        System.out.println(demoService.sum(1, 2));

        demo2Service.say();
        demoService.demoServiceMethod4TestConsoleLength();
    }
}
