package cn.erika.service;

import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Inject;
import cn.erika.context.annotation.ServiceMapping;
import cn.erika.socket.model.po.Account;
import cn.erika.socket.orm.IAccountService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("demoService")
public class DemoServiceImpl extends BaseService implements IDemoService {
    private Map<String, Object> map = new HashMap<>();

    @Inject(name = "accountService")
    private IAccountService accountService;

    static {
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
        List<Account> list = accountService.getAll();
        for (Account account : list) {
            System.out.println(account);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) map.get(key);
    }

    public void say(String string) {
        System.out.println(string);
    }

    public void say(Object object) {
        System.out.println(object.toString());
    }
}
