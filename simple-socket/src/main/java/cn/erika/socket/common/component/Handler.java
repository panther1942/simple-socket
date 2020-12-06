package cn.erika.socket.common.component;

import cn.erika.aop.exception.BeanException;

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
    public void init(BaseSocket socket);

    /**
     * 连接建立之后的一些必须要做的操作 比如握手
     *
     * @param socket 对应的Socket对象
     */
    public void onOpen(BaseSocket socket) throws BeanException;

    public void onReady(BaseSocket socket);

    /**
     * Reader解析完数据之后交给handler处理
     *
     * @param socket  数据源的socket对象
     * @param message 数据
     */
    public void onMessage(BaseSocket socket, DataInfo info, Message message) throws BeanException;

    /**
     * 当需要关闭指定Socket连接的时候 调用此方法
     *
     * @param socket 对应的Socket连接
     */
    public void onClose(BaseSocket socket);

    public void onError(String message, Throwable error);
}
