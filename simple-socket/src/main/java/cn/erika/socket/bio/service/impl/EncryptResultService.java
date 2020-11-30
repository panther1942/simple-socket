package cn.erika.socket.bio.service.impl;

import cn.erika.socket.bio.core.TcpSocket;
import cn.erika.socket.bio.handler.AbstractHandler;
import cn.erika.socket.bio.handler.Message;
import cn.erika.socket.Constant;
import cn.erika.socket.bio.service.ISocketService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EncryptResultService implements ISocketService {
    private static final String serviceName = Constant.SEVR_ENCRYPT_RESULT;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(AbstractHandler handler, TcpSocket socket, Message message) {
        Map<String, String> result = JSON.parseObject(message.getPayload(), Map.class);
        String code = result.get("code");
        String msg = result.get("message");
        if ("0".equals(code)) {
            log.info(msg);
            socket.set(Constant.ENCRYPT, true);
        } else {
            log.warn(msg);
            socket.set(Constant.ENCRYPT, false);
        }
    }

    @Override
    public void server(AbstractHandler handler, TcpSocket socket, Message message) {

    }
}
