package cn.erika.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
// 标记需要使用Application管理实例的组件
public @interface Component {
    public String value() default "";

    public Type type() default Type.SingleTon;

    public boolean ignore() default false;

    public enum Type {
        SingleTon(),
        ProtoType();
    }
}