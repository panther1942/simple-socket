package cn.erika.socket.services.impl.basicAuth;

import cn.erika.aop.ExchangeExceptionHandler;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.security.SecurityAlgorithm;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.string.StringUtils;

/**
 * 基础安全组件 用于确保通信安全
 * <p>
 * 这一步用于在已有的连接上新开子连接的认证
 */
@Component(Constant.SRV_EXCHANGE_TOKEN)
public class ExchangeToken extends BaseService implements ISocketService {

    @Enhance(ExchangeExceptionHandler.class)
    @Override
    public void client(ISocket socket, Message message) throws AuthenticateException {
        // 认证由客户端发起 不能被动发起
        if (message == null) {
            message = new Message(Constant.SRV_EXCHANGE_TOKEN);
            // 发送token和公钥 供服务器识别身份
            message.add(Constant.TOKEN, socket.get(Constant.TOKEN));
            message.add(Constant.PUBLIC_KEY, encoder.encodeToString(GlobalSettings.publicKey));
            socket.send(message);
        } else {
            log.info("加密协商成功");
            // 这里的对称加密由服务器定
            byte[] bSecurityAlgorithm = message.get(Constant.SECURITY_ALGORITHM);
            byte[] bSecurityKey = message.get(Constant.SECURITY_KEY);
            byte[] bSecurityIv = message.get(Constant.SECURITY_IV);

            if (bSecurityAlgorithm == null || bSecurityKey == null) {
                throw new AuthenticateException("缺少加密信息");
            }
            byte[] privateKey = GlobalSettings.privateKey;
            // 算法
            SecurityAlgorithm securityAlgorithm = SecurityUtils.getSecurityAlgorithmByValue(
                    decryptWithRsaToString(bSecurityAlgorithm, privateKey));
            // 密钥
            String securityKey = decryptWithRsaToString(bSecurityKey, privateKey);

            socket.set(Constant.SECURITY_ALGORITHM, securityAlgorithm);
            socket.set(Constant.SECURITY_KEY, securityKey);

            // 向量
            if (securityAlgorithm.isNeedIv()) {
                if (bSecurityIv == null) {
                    throw new AuthenticateException("加密方式缺少向量");
                }
                byte[] securityIv = decryptWithRsa(bSecurityIv, privateKey);
                socket.set(Constant.SECURITY_IV, securityIv);
            }

            // 告知协商结果
            Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
            reply.add(Constant.RESULT, true);
            reply.add(Constant.TEXT, "加密协商成功");
            socket.send(reply);

            // 设置连接的加密flag
            socket.set(Constant.ENCRYPT, true);
            // 客户端执行任务队列
            socket.getHandler().onReady(socket);
        }
    }

    @Enhance(ExchangeExceptionHandler.class)
    @Override
    public void server(ISocket socket, Message message) throws AuthenticateException {
        try {
            if (message != null) {
                IServer server = getBean(IServer.class);
                String token = message.get(Constant.TOKEN);
                byte[] publicKey = decoder.decode((String) message.get(Constant.PUBLIC_KEY));
                ISocket parent = server.checkToken(token, publicKey);
                boolean isAuthenticated = parent.get(Constant.AUTHENTICATED);
                if (!isAuthenticated) {
                    throw new AuthenticateException("未经认证的连接");
                }
                log.debug("认证通过");
                socket.set(Constant.PARENT_SOCKET, parent);
                socket.set(Constant.PUBLIC_KEY, publicKey);
                socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, parent.get(Constant.DIGITAL_SIGNATURE_ALGORITHM));
                socket.set(Constant.TOKEN, token);

                SecurityAlgorithm securityAlgorithm = GlobalSettings.securityAlgorithm;
                String securityKey = StringUtils.randomString(GlobalSettings.securityLength);
                byte[] securityIv;

                socket.set(Constant.SECURITY_ALGORITHM, securityAlgorithm);
                socket.set(Constant.SECURITY_KEY, securityKey);

                Message reply = new Message(Constant.SRV_EXCHANGE_TOKEN);
                reply.add(Constant.SECURITY_ALGORITHM, encryptWithRsa(securityAlgorithm.getValue(), publicKey));
                reply.add(Constant.SECURITY_KEY, encryptWithRsa(securityKey, publicKey));

                if (securityAlgorithm.isNeedIv()) {
                    securityIv = StringUtils.randomString(securityAlgorithm.getIvLength()).getBytes(charset);
                    socket.set(Constant.SECURITY_IV, securityIv);
                    reply.add(Constant.SECURITY_IV, encryptWithRsa(securityIv, publicKey));
                }
                socket.send(reply);
            }
        } catch (BeanException e) {
            log.error("内部错误 无法获取服务器实例: ");
        }
    }
}
