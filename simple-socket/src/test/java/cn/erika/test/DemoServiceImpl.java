package cn.erika.test;

import cn.erika.aop.annotation.Component;

@Component(value = "demo",type = Component.Type.SingleTon)
public class DemoServiceImpl implements IDemoService{
    @Override
    public void test(String uid) throws InterruptedException {
        System.out.println("start: " + uid);
        System.out.println(this.hashCode());
        Thread.sleep(1000);
        System.out.println("end: " + uid);
    }
}
