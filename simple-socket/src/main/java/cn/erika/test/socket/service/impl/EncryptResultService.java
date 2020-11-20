package cn.erika.test.socket.service.impl;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.Message;
import cn.erika.test.socket.handler.StringDefine;
import cn.erika.test.socket.service.ISocketService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EncryptResultService implements ISocketService {
    private static final String serviceName = StringDefine.SEVR_ENCRYPT_RESULT;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void request(AbstractHandler handler, TcpSocket socket, Message message) {

    }

    @Override
    public void response(AbstractHandler handler, TcpSocket socket, Message message) {
        Map<String, String> result = JSON.parseObject(message.getPayload(), Map.class);
        String code = result.get("code");
        String msg = result.get("message");
        if ("0".equals(code)) {
            log.info(msg);
            socket.set(StringDefine.ENCRYPT, true);
        } else {
            log.warn(msg);
            socket.set(StringDefine.ENCRYPT, false);
        }
    }
}
