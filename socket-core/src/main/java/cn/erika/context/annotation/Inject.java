package cn.erika.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标记被Component注解标记的类中需要自动注入的属性
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
    /**
     * 根据服务名称自动注入
     *
     * @return 服务名称
     */
    String name() default "";

    /**
     * 根据类名自动注入
     *
     * @return 服务类名
     */
    Class clazz() default Void.class;
}
