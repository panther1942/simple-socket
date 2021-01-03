package cn.erika.context.bean;

import java.lang.reflect.Method;

// 增强类的接口
public interface Advise {
    // 在执行前
    void before(Method method, Object[] args) throws Throwable;

    // 如果执行成功
    void afterReturning(Method method, Object[] args, Object result);

    // 如果执行失败
    void afterThrowing(Method method, Object[] args, Throwable error);
}
