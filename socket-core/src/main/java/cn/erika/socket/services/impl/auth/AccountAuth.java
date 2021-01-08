package cn.erika.socket.services.impl.auth;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.orm.IAccountService;
import cn.erika.socket.services.ISocketService;

/**
 * 用于用户认证 即登录
 * <p>
 * 客户端不接受认证请求
 */
@Component(Constant.SRV_ACCOUNT_AUTH)
public class AccountAuth extends BaseService implements ISocketService {
    private IAccountService accountService;

    public AccountAuth() throws BeanException {
        this.accountService = getBean("accountService");
    }

    @Override
    public void client(ISocket socket, Message message) {
        if (message != null) {
            // 从控制台获取用户名和密码 需要经Message对象转发过来
            if (message.get(Constant.SERVICE_NAME) == null) {
                String username = message.get(Constant.USERNAME);
                String password = message.get(Constant.PASSWORD);
                // 同时向socket连接设置用户名和密码
                socket.set(Constant.USERNAME, username);
                socket.set(Constant.PASSWORD, password);
                // 向服务器发送认证请求
                Message request = new Message(Constant.SRV_ACCOUNT_AUTH);
                request.add(Constant.USERNAME, username);
                request.add(Constant.PASSWORD, password);
                socket.send(request);
            } else {
                // 如果message对象有服务名 则认为是服务器告知认证结果
                Boolean result = message.get(Constant.RESULT);
                if (result != null && result) {
                    log.info("认证成功");
                    socket.set(Constant.AUTHENTICATED, true);
                    socket.set(Constant.PROMPT, message.get(Constant.PROMPT));
                    GlobalSettings.prompt = message.get(Constant.PROMPT);
                } else {
                    log.warn("认证失败");
                    socket.set(Constant.AUTHENTICATED, false);
                }
            }
        }
    }

    @Override
    public void server(ISocket socket, Message message) {
        String username = message.get(Constant.USERNAME);
        String password = message.get(Constant.PASSWORD);

        Message reply = new Message(Constant.SRV_ACCOUNT_AUTH);
        // 这里只简单的判断一下 以后会加入更复杂的判断 等加入数据库再说
        if (accountService.get4Auth(username, password) != null) {
            socket.set(Constant.AUTHENTICATED, true);
            socket.set(Constant.USERNAME, username);
            socket.set(Constant.PWD, System.getProperty("user.dir"));
            reply.add(Constant.PROMPT, socket.get(Constant.PWD));
            reply.add(Constant.RESULT, true);
        } else {
            socket.set(Constant.AUTHENTICATED, false);
            reply.add(Constant.RESULT, false);
        }
        socket.send(reply);
    }
}
