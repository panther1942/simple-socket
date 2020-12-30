package cn.erika.context.bean;

import java.lang.reflect.Method;

public interface BeanSelector {
    Method getMethod(Class<?> clazz) throws NoSuchMethodException;
}
