package cn.erika.socket.aop;

import cn.erika.context.annotation.Component;
import cn.erika.context.bean.Advise;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Date;

@Component
public class CountFileTransTimeAspect implements Advise {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Date start;

    @Override
    public void before(Method method, Object[] args) {
        start = new Date();
    }

    @Override
    public void afterReturning(Method method, Object[] args, Object result) {
        Date end = new Date();
        log.info("传输用时: " + (end.getTime() - start.getTime()) / 1000 + "秒");
    }

    @Override
    public void afterThrowing(Method method, Object[] args, Throwable error) {

    }
}
