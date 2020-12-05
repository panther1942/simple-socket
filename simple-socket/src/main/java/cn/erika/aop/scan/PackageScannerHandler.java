package cn.erika.aop.scan;

// 扫包处理器接口
public interface PackageScannerHandler {
    // 过滤
    public boolean filter(Class<?> clazz);

    // 处理
    public void deal(Class<?> clazz);
}
