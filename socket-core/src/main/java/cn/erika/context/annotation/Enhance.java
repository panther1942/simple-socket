package cn.erika.context.annotation;

import cn.erika.context.bean.Advise;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
// 用来标记要使用的增强器
public @interface Enhance {
    Class<? extends Advise> value();
}
