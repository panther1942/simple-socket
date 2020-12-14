package cn.erika.test;

import cn.erika.aop.annotation.Component;

@Component("demo")
public class DemoServiceImpl implements IDemoService{
    @Override
    public void test(String uid) throws InterruptedException {
        System.out.println("start: " + uid);
        Thread.sleep(1000);
        System.out.println("end: " + uid);
    }
}
