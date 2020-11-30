package cn.erika.socket.bio.core;

/**
 * 规定一些处理器必须要做的动作
 */
public interface Handler {

    /**
     * 初始化连接
     * 读取配置文件设置属性
     *
     * @param socket 要初始化的Socket对象
     */
    public void init(TcpSocket socket);

    /**
     * 连接建立之后的一些必须要做的操作 比如握手
     *
     * @param socket 对应的Socket对象
     */
    public void onOpen(TcpSocket socket);

    /**
     * Reader解析完数据之后交给handler处理
     *
     * @param socket 数据源的socket对象
     * @param data   数据
     */
    public void onMessage(TcpSocket socket, byte[] data, DataInfo info);

    /**
     * 当需要关闭指定Socket连接的时候 调用此方法
     *
     * @param socket 对应的Socket连接
     */
    public void onClose(TcpSocket socket);

    public void onError(String message, Throwable error);
}
