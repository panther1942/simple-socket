package cn.erika.aop.annotation;

import cn.erika.aop.bean.Advice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
// 用来标记要使用的增强器
public @interface Aspect {
    public Class<? extends Advice> value();
}
