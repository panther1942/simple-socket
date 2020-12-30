package cn.erika.context.scan;

// 扫包处理器接口
public interface PackageScannerHandler {
    // 过滤
    boolean filter(Class<?> clazz);

    // 处理
    void deal(Class<?> clazz);
}
