package cn.erika.socket.advice;

import cn.erika.aop.component.Advice;

import java.lang.reflect.Method;
import java.util.Date;

public class FileAdvice implements Advice {

    private Date start;

    @Override
    public boolean before(Method method, Object[] args) {
        start = new Date();
        return true;
    }

    @Override
    public void success(Method method, Object[] args, Object result) {
        Date end = new Date();
        System.out.println("传输用时: " + (end.getTime() - start.getTime()) / 1000 + "秒");
    }

    @Override
    public Object failed(Method method, Object[] args, Throwable error) {
        return null;
    }

    @Override
    public void finished(Method method, Object[] args) {

    }

    @Override
    public Object cancel(Method method, Object[] args) {
        return null;
    }
}
