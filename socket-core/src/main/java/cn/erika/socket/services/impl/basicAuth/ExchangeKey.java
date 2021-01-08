package cn.erika.socket.services.impl.basicAuth;

import cn.erika.aop.ExchangeExceptionHandler;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.socket.services.ISocketService;

/**
 * 基础安全组件 用于确保通信安全
 * <p>
 * 这一步用于交换公钥和协商RSA通信
 */
@Component(Constant.SRV_EXCHANGE_KEY)
public class ExchangeKey extends BaseService implements ISocketService {

    @Override
    public void client(ISocket socket, Message message) {
        // 向客户端请求公钥的时候不需要请求体 message只需要放上order就能获取客户端的公钥
        if (message == null) {
            Message request = new Message(Constant.SRV_EXCHANGE_KEY);
            // 客户端公钥
            request.add(Constant.PUBLIC_KEY, encoder.encode(GlobalSettings.publicKey));
            log.debug("向服务器提交加密请求");
            socket.send(request);
        }
    }

    @Enhance(ExchangeExceptionHandler.class)
    @Override
    public void server(ISocket socket, Message message) throws AuthenticateException {
        byte[] clientPublicKey = decoder.decode((byte[]) message.get(Constant.PUBLIC_KEY));
        if (clientPublicKey == null) {
            throw new AuthenticateException("缺少公钥信息");
        }
        // 客户端的公钥
        socket.set(Constant.PUBLIC_KEY, clientPublicKey);
        // 数字签名算法由服务器定
        socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, GlobalSettings.signAlgorithm);
        // 回应服务器的公钥和数字签名算法
        // 因为不对称加密长度限制 因此明文传输
        // 1024 / 8 - 11 = 117 字节 RSA1024公钥 216字节
        // 2048 / 8 - 11 = 245 字节 RSA2048公钥 392字节
        Message reply = new Message(Constant.SRV_EXCHANGE_PASSWORD);
        reply.add(Constant.UID, socket.get(Constant.UID));
        reply.add(Constant.PUBLIC_KEY,
                encoder.encode(GlobalSettings.publicKey));
        reply.add(Constant.DIGITAL_SIGNATURE_ALGORITHM,
                GlobalSettings.signAlgorithm.getValue());
        log.debug("客户端请求加密通信 使用本地签名算法: " + GlobalSettings.signAlgorithm);
        socket.send(reply);
    }
}
