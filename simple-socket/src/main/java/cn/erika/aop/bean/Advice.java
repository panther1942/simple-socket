package cn.erika.aop.bean;

import java.lang.reflect.Method;

// 增强类的接口
public interface Advice {
    // 在执行前
    public boolean before(Method method, Object[] args);

    // 如果执行成功
    public void success(Method method, Object[] args, Object result);

    // 如果执行失败
    public void failed(Method method, Object[] args, Throwable error);

    // 在执行后
    public void finished(Method method, Object[] args);
}
