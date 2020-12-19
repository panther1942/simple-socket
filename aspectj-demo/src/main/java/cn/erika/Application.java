package cn.erika;

import cn.erika.service.IDemoService;
import cn.erika.service.impl.DemoServiceImpl;

public class Application {
    public static void main(String[] args) {
        IDemoService service = new DemoServiceImpl();
        System.out.println(service.sum(1, 2));
    }
}
