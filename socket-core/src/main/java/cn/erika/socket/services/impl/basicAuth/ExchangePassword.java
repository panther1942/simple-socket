package cn.erika.socket.services.impl.basicAuth;

import cn.erika.aop.ExchangeExceptionHandler;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.socket.exception.UnsupportedAlgorithmException;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.security.DigitalSignatureAlgorithm;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.security.algorithm.BasicMessageDigestAlgorithm;
import cn.erika.utils.security.MessageDigestUtils;
import cn.erika.utils.security.SecurityAlgorithm;
import cn.erika.utils.string.StringUtils;

/**
 * 基础安全组件 用于确保通信安全
 * <p>
 * 这一步用于协商对称加密 (现在是客户端指定 服务器接受 以后会改)
 */
@Component(Constant.SRV_EXCHANGE_PASSWORD)
public class ExchangePassword extends BaseService implements ISocketService {

    @Enhance(ExchangeExceptionHandler.class)
    @Override
    public void client(ISocket socket, Message message) throws AuthenticateException {
        // 服务器公钥
        byte[] serverPublicKey = decoder.decode((byte[]) message.get(Constant.PUBLIC_KEY));
        if (serverPublicKey == null) {
            throw new AuthenticateException("缺少公钥信息");
        }
        // 数字加密算法
        DigitalSignatureAlgorithm digitalSignatureAlgorithm = SecurityUtils.getDigitalSignatureAlgorithmByValue(
                message.get(Constant.DIGITAL_SIGNATURE_ALGORITHM)
        );

        // 打印一下信息
        try {
            byte[] keySign = MessageDigestUtils.sum(serverPublicKey, BasicMessageDigestAlgorithm.MD5);
            log.debug("获取服务器公钥 签名信息（MD5）: " +
                    StringUtils.byte2HexString(keySign) + "\n签名算法: " + digitalSignatureAlgorithm);
        } catch (UnsupportedAlgorithmException e) {
            log.warn(e.getMessage());
        }

        // 设置连接的服务器公钥和数字加密信息
        socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, digitalSignatureAlgorithm);
        socket.set(Constant.PUBLIC_KEY, serverPublicKey);

        // 对称加密算法和密钥由客户端定
        SecurityAlgorithm securityAlgorithm = GlobalSettings.securityAlgorithm;
        String securityKey = StringUtils.randomString(GlobalSettings.securityLength);

        socket.set(Constant.SECURITY_ALGORITHM, securityAlgorithm);
        socket.set(Constant.SECURITY_KEY, securityKey);

        log.debug("发送会话密钥，类型:" + securityAlgorithm.getValue() + ", 密钥:" + securityKey);
        // 向服务器发送会话密钥和算法信息 用服务器公钥分别加密
        Message request = new Message(Constant.SRV_EXCHANGE_PASSWORD);
        request.add(Constant.SECURITY_ALGORITHM, encryptWithRsa(securityAlgorithm.getValue(), serverPublicKey));
        request.add(Constant.SECURITY_KEY, encryptWithRsa(securityKey, serverPublicKey));
        // 如果该算法需要向量 则生成向量
        if (securityAlgorithm.isNeedIv()) {
            byte[] securityIv = StringUtils.randomString(securityAlgorithm.getIvLength()).getBytes(charset);
            socket.set(Constant.SECURITY_IV, securityIv);
            request.add(Constant.SECURITY_IV, encryptWithRsa(securityIv, serverPublicKey));
        }
        socket.send(request);
    }

    @Enhance(ExchangeExceptionHandler.class)
    @Override
    public void server(ISocket socket, Message message) throws AuthenticateException {
        //获取客户端发来的对称加密算法,密钥和向量(如果有)
        byte[] bSecurityAlgorithm = message.get(Constant.SECURITY_ALGORITHM);
        byte[] bSecurityKey = message.get(Constant.SECURITY_KEY);
        byte[] bSecurityIv = message.get(Constant.SECURITY_IV);

        if (bSecurityAlgorithm == null || bSecurityKey == null) {
            throw new AuthenticateException("缺少加密信息");
        }
        // 算法
        String securityAlgorithmName = decryptWithRsaToString(bSecurityAlgorithm, GlobalSettings.privateKey);
        SecurityAlgorithm securityAlgorithm = SecurityUtils.getSecurityAlgorithmByValue(securityAlgorithmName);
        if (securityAlgorithm == null) {
            throw new AuthenticateException("不支持的加密算法: " + securityAlgorithmName);
        }
        // 密钥
        String securityKey = decryptWithRsaToString(bSecurityKey, GlobalSettings.privateKey);

        socket.set(Constant.SECURITY_ALGORITHM, securityAlgorithm);
        socket.set(Constant.SECURITY_KEY, securityKey);

        // 向量
        if (securityAlgorithm.isNeedIv()) {
            if (bSecurityIv == null) {
                throw new AuthenticateException("加密方式缺少向量");
            }
            byte[] securityIv = decryptWithRsa(bSecurityIv, GlobalSettings.privateKey);
            socket.set(Constant.SECURITY_IV, securityIv);
        }
        log.debug("收到会话密钥，类型:" + securityAlgorithm + ", 密钥:" + securityKey);
        log.debug("加密协商完成");

        //告知客户端协商结果
        Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
        reply.add(Constant.RESULT, true);
        reply.add(Constant.TEXT, "加密协商成功");
        socket.send(reply);
        // 发送完消息再设置加密flag
        socket.set(Constant.ENCRYPT, true);
        // 服务器执行任务队列
        socket.getHandler().onReady(socket);
    }
}
