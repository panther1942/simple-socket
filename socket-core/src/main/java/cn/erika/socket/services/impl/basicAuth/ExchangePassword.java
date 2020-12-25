package cn.erika.socket.services.impl.basicAuth;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.SocketService;
import cn.erika.util.exception.SerialException;
import cn.erika.util.security.*;
import cn.erika.util.string.SerialUtils;
import cn.erika.util.string.StringUtils;

@Component(Constant.SRV_EXCHANGE_PASSWORD)
public class ExchangePassword extends BaseService implements SocketService {

    @Override
    public void client(ISocket socket, Message message) {
        try {
            byte[] serverPublicKey = message.get(Constant.PUBLIC_KEY);
            if (serverPublicKey == null) {
                throw new SecurityException("缺少公钥信息");
            }
            byte[] serverPublicKeySign = MessageDigest.sum(serverPublicKey, MessageDigestAlgorithm.SHA1);
//            log.debug("获取服务器公钥: " + Base64.getEncoder().encodeToString(serverPublicKey));
            log.debug("获取服务器公钥: " + StringUtils.byteToHexString(serverPublicKeySign));
            socket.set(Constant.PUBLIC_KEY, serverPublicKey);

            SecurityAlgorithm securityAlgorithm = GlobalSettings.securityAlgorithm;
            String securityKey = StringUtils.randomString(GlobalSettings.securityLength);

            socket.set(Constant.SECURITY_ALGORITHM, securityAlgorithm);
            socket.set(Constant.SECURITY_KEY, securityKey);

            log.debug("发送会话密钥，类型:" + securityAlgorithm.getValue() + ", 密钥:" + securityKey);
            Message request = new Message(Constant.SRV_EXCHANGE_PASSWORD);

            request.add(Constant.SECURITY_ALGORITHM,
                    SecurityUtils.encrypt(SerialUtils.serialObject(securityAlgorithm), serverPublicKey));
            request.add(Constant.SECURITY_KEY,
                    SecurityUtils.encrypt(SerialUtils.serialObject(securityKey), serverPublicKey));

            if (securityAlgorithm.isNeedIv()) {
                byte[] securityIv = StringUtils.randomByte(8);
                socket.set(Constant.SECURITY_IV, securityIv);
                request.add(Constant.SECURITY_IV, SecurityUtils.encrypt(securityIv, serverPublicKey));
            }
            socket.send(request);
        } catch (SerialException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void server(ISocket socket, Message message) {
        try {
            byte[] bSecurityAlgorithm = message.get(Constant.SECURITY_ALGORITHM);
            byte[] bSecurityKey = message.get(Constant.SECURITY_KEY);
            byte[] bSecurityIv = message.get(Constant.SECURITY_IV);

            if (bSecurityAlgorithm == null || bSecurityKey == null) {
                throw new SecurityException("缺少加密信息");
            }
            SecurityAlgorithm securityAlgorithm = SerialUtils.serialObject(
                    SecurityUtils.decrypt(bSecurityAlgorithm, GlobalSettings.privateKey));
            String securityKey = SerialUtils.serialObject(
                    SecurityUtils.decrypt(bSecurityKey, GlobalSettings.privateKey));

            socket.set(Constant.SECURITY_ALGORITHM, securityAlgorithm);
            socket.set(Constant.SECURITY_KEY, securityKey);

            if (securityAlgorithm.isNeedIv()) {
                if (bSecurityIv == null) {
                    throw new SecurityException("加密方式缺少向量");
                }
                byte[] securityIv = SecurityUtils.decrypt(bSecurityIv, GlobalSettings.privateKey);
                socket.set(Constant.SECURITY_IV, securityIv);
            }
            log.debug("收到会话密钥，类型:" + securityAlgorithm + ", 密钥:" + securityKey);
            log.debug("加密协商完成");
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
    }
}
