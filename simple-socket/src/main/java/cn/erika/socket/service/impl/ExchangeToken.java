package cn.erika.socket.service.impl;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.socket.common.exception.TokenException;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.service.ISocketService;

@Component(Constant.SRV_EXCHANGE_TOKEN)
public class ExchangeToken implements ISocketService {
    @Override
    public void client(BaseSocket socket, Message message) {
        if (message == null) {
            message = new Message(Constant.SRV_EXCHANGE_TOKEN);
            message.add(Constant.SESSION_TOKEN, socket.get(Constant.SESSION_TOKEN));
            message.add(Constant.PUBLIC_KEY, socket.get(Constant.PUBLIC_KEY));
            socket.send(message);
        }else{
            socket.ready();
        }
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        try {
            if (message != null) {
                IServer server = App.getBean(IServer.class);
                String token = message.get(Constant.SESSION_TOKEN);
                String publicKey = message.get(Constant.PUBLIC_KEY);
                try {
                    server.checkToken(token, publicKey);
                    // 如果这里发生server的空指针异常说明服务器未启动 但是这是不可能的
                    Message reply = new Message(Constant.SRV_EXCHANGE_TOKEN);
                    socket.send(reply);
                } catch (TokenException e) {
                    e.printStackTrace();
                }
            }
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }
}