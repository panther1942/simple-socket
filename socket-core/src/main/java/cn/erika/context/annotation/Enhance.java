package cn.erika.context.annotation;

import cn.erika.context.bean.Advise;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标记要使用的增强器
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Enhance {
    /**
     * 要使用的增强器的类名 增强器需要使用Component注解以自动实例化对象
     *
     * @return 增强器的类名
     */
    Class<? extends Advise> value();
}
