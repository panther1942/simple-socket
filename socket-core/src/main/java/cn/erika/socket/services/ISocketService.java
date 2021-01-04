package cn.erika.socket.services;

import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.Message;

/**
 * socket服务都必须实现这个接口
 * 实现两个方法对应客户端和服务器的不同动作
 */
public interface ISocketService {

    void client(ISocket socket, Message message) throws Throwable;

    void server(ISocket socket, Message message) throws Throwable;
}
