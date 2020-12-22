package cn.erika.service;

import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.ServiceMapping;

@Component
public class DemoServiceImpl extends BaseService implements IDemoService {

    @ServiceMapping("sum")
    @Override
    public int sum(int a, int b) {
        return super.sum(a, b);
    }

    @Override
    public void say() {
        System.out.println("Hello World");
    }
}
