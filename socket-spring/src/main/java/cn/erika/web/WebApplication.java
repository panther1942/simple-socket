package cn.erika.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.swing.*;

@SpringBootApplication
public class WebApplication extends SpringApplication{
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(WebApplication.class);
        ConfigurableApplicationContext ctx = application.run(args);
//        ctx.getBean();
    }
}
