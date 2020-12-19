package cn.erika.service.impl;

import cn.erika.service.IDemoService;

public class DemoServiceImpl implements IDemoService{
    @Override
    public int sum(int a, int b) {
        return a + b;
    }
}
