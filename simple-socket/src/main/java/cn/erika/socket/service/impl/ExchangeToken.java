package cn.erika.socket.service.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.exception.TokenException;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.service.ISocketService;
import cn.erika.util.security.RSA;
import cn.erika.util.security.Security;
import cn.erika.util.security.SecurityException;
import cn.erika.util.string.SerialUtils;
import cn.erika.util.string.StringUtils;

import java.io.IOException;

@SocketServiceMapping(Constant.SRV_EXCHANGE_TOKEN)
public class ExchangeToken implements ISocketService {
    @Override
    public void client(BaseSocket socket, Message message) {
        if (message == null) {
            message = new Message(Constant.SRV_EXCHANGE_TOKEN);
            message.add(Constant.SESSION_TOKEN, socket.get(Constant.SESSION_TOKEN));
            message.add(Constant.PUBLIC_KEY, GlobalSettings.publicKey);
            socket.send(message);
        } else {
            try {
                Security.Type passwordType = SerialUtils.serialObject(RSA.decryptByPrivateKey(
                        message.get(Constant.SECURITY_NAME), GlobalSettings.privateKey
                ));
                String password = SerialUtils.serialObject(RSA.decryptByPrivateKey(
                        message.get(Constant.SECURITY_CODE), GlobalSettings.privateKey
                ));
                socket.set(Constant.SECURITY_NAME, passwordType);
                socket.set(Constant.SECURITY_CODE, password);
                socket.set(Constant.ENCRYPT, true);
                socket.ready();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        try {
            if (message != null) {
                IServer server = App.getBean(IServer.class);
                String token = message.get(Constant.SESSION_TOKEN);
                byte[] publicKey = message.get(Constant.PUBLIC_KEY);
                try {
                    BaseSocket parent = server.checkToken(token, publicKey);
                    socket.set(Constant.PARENT_SOCKET, parent);
                    socket.set(Constant.PUBLIC_KEY, publicKey);
                    socket.set(Constant.RSA_ALGORITHM, parent.get(Constant.RSA_ALGORITHM));
                    socket.set(Constant.SESSION_TOKEN, token);
                    Message reply = new Message(Constant.SRV_EXCHANGE_TOKEN);
                    Security.Type passwordType = GlobalSettings.passwordType;
                    String password = StringUtils.randomString(GlobalSettings.passwordLength);
                    socket.set(Constant.SECURITY_NAME, passwordType);
                    socket.set(Constant.SECURITY_CODE, password);
                    reply.add(Constant.SECURITY_CODE, RSA.encryptByPublicKey(
                            SerialUtils.serialObject(password), publicKey
                    ));
                    reply.add(Constant.SECURITY_NAME, RSA.encryptByPublicKey(
                            SerialUtils.serialObject(passwordType), publicKey
                    ));
                    socket.send(reply);
                    socket.set(Constant.ENCRYPT, true);
                } catch (TokenException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }
}
