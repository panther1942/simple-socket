package cn.erika.socket.services.impl.auth;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.TokenException;
import cn.erika.socket.handler.Server;
import cn.erika.socket.services.SocketService;
import cn.erika.util.exception.SerialException;
import cn.erika.util.security.SecurityAlgorithm;
import cn.erika.util.string.StringUtils;

@Component(Constant.SRV_EXCHANGE_TOKEN)
public class ExchangeToken extends BaseService implements SocketService {

    @Override
    public void client(Socket socket, Message message) {
        if (message == null) {
            message = new Message(Constant.SRV_EXCHANGE_TOKEN);
            message.add(Constant.TOKEN, socket.get(Constant.TOKEN));
            message.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
            socket.send(message);
        } else {
            if (message.get(Constant.RESULT).equals(Constant.SUCCESS)) {
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
                    log.error("加密协商失败");
                    Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
                    reply.add(Constant.RESULT, false);
                    reply.add(Constant.TEXT, "加密协商失败");
                    socket.send(reply);
                    socket.close();
                } catch (SecurityException e) {
                    log.error("加密协商失败");
                    Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
                    reply.add(Constant.RESULT, false);
                    reply.add(Constant.TEXT, e.getMessage());
                    socket.send(reply);
                    socket.close();
                }
            } else {
                throw new SecurityException("认证失败");
            }
        }
    }

    @Override
    public void server(Socket socket, Message message) {
        try {
            if (message != null) {
                Server server = getBean(Server.class);
                String token = message.get(Constant.TOKEN);
                byte[] publicKey = message.get(Constant.PUBLIC_KEY);
                try {
                    Socket parent = server.checkToken(token, publicKey);
                    log.debug("认证通过");
                    socket.set(Constant.PARENT_SOCKET, parent);
                    socket.set(Constant.PUBLIC_KEY, publicKey);
                    socket.set(Constant.DIGITAL_SIGNATURE_ALGORITHM, parent.get(Constant.DIGITAL_SIGNATURE_ALGORITHM));
                    socket.set(Constant.TOKEN, token);

                    SecurityAlgorithm securityAlgorithm = GlobalSettings.securityAlgorithm;
                    String securityKey = StringUtils.randomString(GlobalSettings.securityLength);
                    byte[] securityIv = null;

                    socket.set(Constant.SECURITY_ALGORITHM, securityAlgorithm);
                    socket.set(Constant.SECURITY_KEY, securityKey);

                    Message reply = new Message(Constant.SRV_EXCHANGE_TOKEN);
                    reply.add(Constant.SECURITY_ALGORITHM, encryptWithRsa(securityAlgorithm, publicKey));
                    reply.add(Constant.SECURITY_KEY, encryptWithRsa(securityKey, publicKey));

                    if (securityAlgorithm.isNeedIv()) {
                        securityIv = StringUtils.randomByte(8);
                        socket.set(Constant.SECURITY_IV, securityIv);
                        reply.add(Constant.SECURITY_IV, encryptWithRsa(securityIv, publicKey));
                    }
                    reply.add(Constant.RESULT, Constant.SUCCESS);
                    socket.send(reply);
                } catch (TokenException e) {
                    log.warn("认证失败");
                    Message reply = new Message(Constant.SRV_EXCHANGE_TOKEN);
                    socket.set(Constant.RESULT, Constant.FAILED);
                    socket.send(reply);
                    socket.close();
                } catch (SerialException e) {
                    e.printStackTrace();
                }
            }
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }
}
