package cn.erika.context.bean;

import java.lang.reflect.Method;

// 增强类的接口
public interface Advise {
    // 在执行前
    public void before(Method method, Object[] args);

    // 如果执行成功
    public void afterReturning(Method method, Object[] args, Object result);

    // 如果执行失败
    public void afterThrowing(Method method, Object[] args, Throwable error);
}
