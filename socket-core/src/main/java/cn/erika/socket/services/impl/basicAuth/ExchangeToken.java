package cn.erika.socket.services.impl.basicAuth;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.services.ISocketService;
import cn.erika.util.exception.SerialException;
import cn.erika.util.security.SecurityAlgorithm;
import cn.erika.util.string.StringUtils;

@Component(Constant.SRV_EXCHANGE_TOKEN)
public class ExchangeToken extends BaseService implements ISocketService {

    @Override
    public void client(ISocket socket, Message message) {
        if (message == null) {
            message = new Message(Constant.SRV_EXCHANGE_TOKEN);
            message.add(Constant.TOKEN, socket.get(Constant.TOKEN));
            message.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
            socket.send(message);
        } else {
            boolean result = message.get(Constant.RESULT);
            try {
                if (result) {
                    log.info("加密协商成功");
                    byte[] bSecurityAlgorithm = message.get(Constant.SECURITY_ALGORITHM);
                    byte[] bSecurityKey = message.get(Constant.SECURITY_KEY);
                    byte[] bSecurityIv = message.get(Constant.SECURITY_IV);

                    if (bSecurityAlgorithm == null || bSecurityKey == null) {
                        throw new SecurityException("缺少加密信息");
                    }
                    byte[] privateKey = GlobalSettings.privateKey;
                    try {
                        SecurityAlgorithm securityAlgorithm = decryptWithRsa(bSecurityAlgorithm, privateKey);
                        String securityKey = decryptWithRsa(bSecurityKey, privateKey);
                        socket.set(Constant.SECURITY_ALGORITHM, securityAlgorithm);
                        socket.set(Constant.SECURITY_KEY, securityKey);

                        if (securityAlgorithm.isNeedIv()) {
                            if (bSecurityIv == null) {
                                throw new SecurityException("加密方式缺少向量");
                            }
                            byte[] securityIv = decryptWithRsa(bSecurityIv, privateKey);
                            socket.set(Constant.SECURITY_IV, securityIv);
                        }
                        Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
                        reply.add(Constant.RESULT, true);
                        reply.add(Constant.TEXT, "加密协商成功");
                        socket.send(reply);
                        socket.set(Constant.ENCRYPT, true);
                    } catch (SerialException e) {
                        log.error("序列化错误: " + e.getMessage());
                    } catch (SecurityException e) {
                        throw new AuthenticateException("加密协商失败: " + e.getMessage());
                    }
                } else {
                    throw new AuthenticateException("认证失败");
                }
            } catch (AuthenticateException e) {
                log.error("加密协商失败");
                Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
                reply.add(Constant.RESULT, false);
                reply.add(Constant.TEXT, e.getMessage());
                socket.send(reply);
                socket.close();
            }
        }
    }

    @Override
    public void server(ISocket socket, Message message) {
        try {
            if (message != null) {
                IServer server = getBean(IServer.class);
                String token = message.get(Constant.TOKEN);
                byte[] publicKey = message.get(Constant.PUBLIC_KEY);
                try {
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
                    reply.add(Constant.SECURITY_ALGORITHM, encryptWithRsa(securityAlgorithm, publicKey));
                    reply.add(Constant.SECURITY_KEY, encryptWithRsa(securityKey, publicKey));

                    if (securityAlgorithm.isNeedIv()) {
                        securityIv = StringUtils.randomByte(securityAlgorithm.getIvLength());
                        socket.set(Constant.SECURITY_IV, securityIv);
                        reply.add(Constant.SECURITY_IV, encryptWithRsa(securityIv, publicKey));
                    }
                    reply.add(Constant.RESULT, true);
                    socket.send(reply);
                } catch (AuthenticateException e) {
                    log.warn("认证失败: " + e.getMessage());
                    Message reply = new Message(Constant.SRV_EXCHANGE_TOKEN);
                    reply.add(Constant.RESULT, false);
                    reply.add(Constant.TEXT, e.getMessage());
                    socket.send(reply);
                    socket.close();
                } catch (SerialException e) {
                    log.error("序列化错误: " + e.getMessage());
                }
            }
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }
}
