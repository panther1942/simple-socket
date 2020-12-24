package cn.erika.web.service.impl;

import cn.erika.web.service.IDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DemoServiceImpl implements IDemoService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public int sum(int a, int b) {
        return a + b;
    }

    @Override
    public void demoServiceMethod4TestConsoleLength() {
        log.debug(Thread.currentThread().getName()+"!!!");
//        Thread.getDefaultUncaughtExceptionHandler().
    }
}
