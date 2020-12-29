package cn.erika.socket.services.impl.basicAuth;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ISocketService;

@Component(Constant.SRV_EXCHANGE_KEY)
public class ExchangeKey extends BaseService implements ISocketService {

    @Override
    public void client(ISocket socket, Message message) {
        // 向客户端请求公钥的时候不需要请求体 message只需要放上order就能获取客户端的公钥
        Message request = new Message(Constant.SRV_EXCHANGE_KEY);
        // 客户端公钥
        request.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
        // 签名验证方式
//        request.add(Constant.DIGITAL_SIGNATURE_ALGORITHM, GlobalSettings.signAlgorithm);
        log.debug("向服务器请求加密 签名类型: " + GlobalSettings.signAlgorithm);
        socket.send(request);
    }

    @Override
    public void server(ISocket socket, Message message) {
        byte[] clientPublicKey = message.get(Constant.PUBLIC_KEY);
        if (clientPublicKey == null) {
            throw new SecurityException("缺少公钥信息");
        }
        socket.set(Constant.PUBLIC_KEY, clientPublicKey);
        socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, GlobalSettings.signAlgorithm);
        Message reply = new Message(Constant.SRV_EXCHANGE_PASSWORD);
        reply.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
        reply.add(Constant.DIGITAL_SIGNATURE_ALGORITHM, GlobalSettings.signAlgorithm);
        log.debug("客户端请求加密通信 使用本地签名算法: " + GlobalSettings.signAlgorithm);
        socket.send(reply);
    }
}
