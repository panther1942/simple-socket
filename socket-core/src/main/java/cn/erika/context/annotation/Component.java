package cn.erika.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
// 标记需要使用Application管理实例的组件
public @interface Component {
    String value() default "";

    Type type() default Type.SingleTon;

    boolean ignore() default false;

    enum Type {
        SingleTon(),
        ProtoType()
    }
}
