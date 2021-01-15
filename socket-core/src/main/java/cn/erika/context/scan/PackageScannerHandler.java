package cn.erika.context.scan;

/**
 * 扫包处理器 不要在这里执行类的实例化操作 因为大概率会出现找不到类的错误
 */
public interface PackageScannerHandler {
    // 过滤方法
    boolean filter(Class<?> clazz);

    // 处理处理方法 只有filter返回为true的时候才会执行此方法
    void deal(Class<?> clazz);
}
