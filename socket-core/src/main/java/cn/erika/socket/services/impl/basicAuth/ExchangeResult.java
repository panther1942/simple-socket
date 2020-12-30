package cn.erika.socket.services.impl.basicAuth;

import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ISocketService;

/**
 * 基础安全组件 用于确保通信安全
 * <p>
 * 这一步用于通知协商结果
 */
@Component(Constant.SRV_EXCHANGE_RESULT)
public class ExchangeResult extends BaseService implements ISocketService {

    @Override
    public void client(ISocket socket, Message message) {
        deal(socket, message);
    }

    @Override
    public void server(ISocket socket, Message message) {
        deal(socket, message);
    }

    private void deal(ISocket socket, Message message) {
        Boolean result = message.get(Constant.RESULT);
        String msg = message.get(Constant.TEXT);
        if (result != null && result) {
            // 协商成功则设置连接的加密flag
            log.info(msg);
            socket.set(Constant.ENCRYPT, true);
        } else {
            // 协商失败 则关闭连接
            log.error(msg);
            socket.close();
        }
    }
}
