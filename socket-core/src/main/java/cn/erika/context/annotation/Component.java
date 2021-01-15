package cn.erika.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标记服务类组件 在扫包的时候会执行扫描范围内包含此注解类的静态方法（class.forName导致的）
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Component {
    /**
     * 服务名称
     * 如果不写服务名称将不会被getBean(String,Object...)方法调用
     * 但仍然可以使用getBean(Class,Object...)方法调用
     * 如果服务名称有重名 将覆盖原先注册的同名服务名称
     *
     * @return 服务名称
     */
    String value() default "";

    /**
     * 服务加载类型 如果是单例则存储该类的实例对象
     *
     * @return 单例为SingleTon 原型为ProtoType
     */
    Type type() default Type.SingleTon;

    /**
     * 是否忽略自动实例化该类的对象
     * 有一些类需要被缓存 但需要手动实例化
     *
     * @return true将被忽略 false将自动实例化对象
     */
    boolean ignore() default false;

    /**
     * 服务加载类型 写成枚举类方便
     */
    enum Type {
        SingleTon(),
        ProtoType()
    }
}
