package cn.erika.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WebApplication extends SpringApplication {
    private static Logger log = LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(WebApplication.class);
        ConfigurableApplicationContext ctx = application.run(args);
//        ctx.getBean();
    }
}
