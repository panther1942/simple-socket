package cn.erika.web.service.impl;

import cn.erika.web.service.IDemoService;
import org.springframework.stereotype.Service;

@Service
public class DemoServiceImpl implements IDemoService {


    @Override
    public int sum(int a, int b) {
        return a + b;
    }
}
