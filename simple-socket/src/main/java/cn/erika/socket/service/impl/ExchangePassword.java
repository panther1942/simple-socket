package cn.erika.socket.service.impl;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.service.ISocketService;
import cn.erika.util.security.RSA;
import cn.erika.util.security.Security;
import cn.erika.util.security.SecurityException;
import cn.erika.util.string.SerialUtils;
import cn.erika.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

@SocketServiceMapping(Constant.SRV_EXCHANGE_PASSWORD)
public class ExchangePassword implements ISocketService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(BaseSocket socket, Message message) {
        try {
            byte[] serverPublicKey = message.get(Constant.PUBLIC_KEY);
            if (serverPublicKey == null) {
                throw new SecurityException("缺少公钥信息");
            }
//            log.debug("获取服务器公钥: " + Base64.getEncoder().encodeToString(serverPublicKey));
            socket.set(Constant.PUBLIC_KEY, serverPublicKey);

            Security.Type passwordType = GlobalSettings.passwordType;
            String password = StringUtils.randomString(GlobalSettings.passwordLength);
            socket.set(Constant.SECURITY_NAME, passwordType);
            socket.set(Constant.SECURITY_CODE, password);
            Message request = new Message(Constant.SRV_EXCHANGE_PASSWORD);
            request.add(Constant.SECURITY_NAME,
                    RSA.encryptByPublicKey(SerialUtils.serialObject(passwordType), serverPublicKey));
            request.add(Constant.SECURITY_CODE,
                    RSA.encryptByPublicKey(SerialUtils.serialObject(
                            Base64.getEncoder().encodeToString(password.getBytes(GlobalSettings.charset))
                    ), serverPublicKey));
            log.debug("发送会话密钥，类型:" + passwordType.getValue() + ", 密钥:" + password);
            socket.send(request);
        } catch (SecurityException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        try {
            Security.Type encryptType = SerialUtils.serialObject(RSA.decryptByPrivateKey(
                    message.get(Constant.SECURITY_NAME),GlobalSettings.privateKey
            ));
            String encryptCode = SerialUtils.serialObject(RSA.decryptByPrivateKey(
                    message.get(Constant.SECURITY_CODE),GlobalSettings.privateKey
            ));
            if (encryptType == null || encryptCode == null) {
                throw new SecurityException("缺少加密信息");
            }
            encryptCode = new String(Base64.getDecoder().decode(encryptCode));
            log.debug("收到会话密钥，类型:" + encryptType + ", 密钥:" + encryptCode);
            log.debug("加密协商完成");
            Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
            reply.add(Constant.RESULT, true);
            reply.add(Constant.MESSAGE, "加密协商成功");
            socket.send(reply);
            socket.set(Constant.SECURITY_CODE, encryptCode);
            socket.set(Constant.SECURITY_NAME, encryptType);
            socket.set(Constant.ENCRYPT, true);
        } catch (SecurityException e) {
            log.error("加密协商失败");
            Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
            reply.add(Constant.RESULT, false);
            reply.add(Constant.MESSAGE, "加密协商失败");
            socket.send(reply);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
