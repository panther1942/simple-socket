package cn.erika.test;

import cn.erika.aop.component.Advice;

import java.lang.reflect.Method;

public class DemoAdviceImpl implements Advice {

    @Override
    public boolean before(Method method, Object[] args) {
        return false;
    }

    @Override
    public void success(Method method, Object[] args, Object result) {

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
