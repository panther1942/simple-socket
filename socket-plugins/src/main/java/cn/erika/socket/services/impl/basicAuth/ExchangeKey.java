package cn.erika.socket.services.impl.basicAuth;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.SocketService;
import cn.erika.util.security.DigitalSignatureAlgorithm;

@Component(Constant.SRV_EXCHANGE_KEY)
public class ExchangeKey extends BaseService implements SocketService {

    @Override
    public void client(Socket socket, Message message) {
        // 向客户端请求公钥的时候不需要请求体 message只需要放上order就能获取客户端的公钥
        Message request = new Message(Constant.SRV_EXCHANGE_KEY);
        request.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
        request.add(Constant.DIGITAL_SIGNATURE_ALGORITHM, GlobalSettings.digitalSignatureAlgorithm);
        log.debug("向服务器请求加密 签名类型: " + GlobalSettings.digitalSignatureAlgorithm);
        socket.send(request);
    }

    @Override
    public void server(Socket socket, Message message) {
        byte[] clientPublicKey = message.get(Constant.PUBLIC_KEY);
        DigitalSignatureAlgorithm digitalSignatureAlgorithm = message.get(Constant.DIGITAL_SIGNATURE_ALGORITHM);
        if (clientPublicKey == null) {
            throw new SecurityException("缺少公钥信息");
        }
        socket.set(Constant.PUBLIC_KEY, clientPublicKey);
        socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, digitalSignatureAlgorithm);
        Message reply = new Message(Constant.SRV_EXCHANGE_PASSWORD);
        reply.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
        log.debug("客户端请求加密通信 签名类型: " + digitalSignatureAlgorithm);
        socket.send(reply);

    }
}
