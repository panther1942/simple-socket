package cn.erika.service;

import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.ServiceMapping;

import java.util.HashMap;
import java.util.Map;

@Component
public class DemoServiceImpl extends BaseService implements IDemoService {
    private Map<String, Object> map = new HashMap<>();

    static{
        System.out.println("静态代码块");
    }

    @ServiceMapping("sum")
    @Override
    public int sum(int a, int b) {
        return super.sum(a, b);
    }

    @Override
    public void say() {
        System.out.println("Hello World");
//        say(get("a"));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) map.get(key);
    }

    public void say(byte[] array){

    }

    public void say(String string) {
        System.out.println(string);
    }

    public void say(Object object) {
        System.out.println(object.toString());
    }
}
