package cn.erika.socket.bio.service.impl;

import cn.erika.socket.bio.core.TcpSocket;
import cn.erika.socket.bio.handler.AbstractHandler;
import cn.erika.socket.bio.handler.Message;
import cn.erika.socket.Constant;
import cn.erika.socket.bio.service.ISocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublicKeyService implements ISocketService {
    private static final String serviceName = Constant.SEVR_PUBLIC_KEY;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(AbstractHandler handler, TcpSocket socket, Message message) {
        // 向客户端请求公钥的时候不需要请求体 message只需要放上order就能获取客户端的公钥
        Message request = new Message(serviceName, AbstractHandler.getPublicKey());
        handler.sendMessage(socket, request);
//        log.debug("发送公钥: " + Base64.getEncoder().encodeToString(AbstractHandler.getPublicKey()));
    }

    @Override
    public void server(AbstractHandler handler, TcpSocket socket, Message message) {
        // 向服务器请求公钥的时候需要携带自己的公钥信息 TODO 这里需要做验证处理 如果没有公钥则拒绝下一步操作
        // TODO 这里需要个统一异常处理方便拓展功能 毕竟把异常处理写的到处都是很不美/爽
        // TODO 还需要个DEBUG功能 到处写log.debug要吐了 写完还要记得删 相当不爽
        socket.set(Constant.PUBLIC_KEY, message.getPayload());
        Message response = new Message(Constant.SEVR_EXCHANGE_KEY, AbstractHandler.getPublicKey());
        handler.sendMessage(socket, response);
        log.debug("对端请求加密通信");
//        log.debug("获得公钥: " + Base64.getEncoder().encodeToString(message.getPayload()));
//        log.debug("发送公钥: " + Base64.getEncoder().encodeToString(AbstractHandler.getPublicKey()));
    }
}
