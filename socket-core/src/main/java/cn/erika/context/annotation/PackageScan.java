package cn.erika.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标记需要扫描的包名 需要在启动类使用
 * 或者使用PackageScanner手动指定并扫描包
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PackageScan {
    /**
     * 指定的包名 可以指定多个
     *
     * @return 指定的包名
     */
    String[] value() default {};
}
