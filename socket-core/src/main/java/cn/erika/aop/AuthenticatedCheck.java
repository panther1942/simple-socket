package cn.erika.aop;

import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.bean.Advise;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.lang.reflect.Method;

@Component
public class AuthenticatedCheck implements Advise {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void before(Method method, Object[] args) throws AuthenticateException {
        Object obj = args[0];
        if (obj instanceof ISocket) {
            ISocket socket = (ISocket) obj;
            boolean authenticated = socket.get(Constant.AUTHENTICATED);
            if (!authenticated) {
                ISocket parent = socket.get(Constant.PARENT_SOCKET);
                if (parent == null || parent.get(Constant.AUTHENTICATED) == null || !(boolean) parent.get(Constant.AUTHENTICATED)) {
                    String type = socket.get(Constant.TYPE);
                    switch (type) {
                        case Constant.CLIENT:
                            throw new AuthenticateException("需要登录后操作");
                        case Constant.SERVER:
                            throw new AuthenticateException("用户没有登录: " + socket.get(Constant.UID));
                    }
                }
            }
        }
    }

    @Override
    public void afterReturning(Method method, Object[] args, Object result) {

    }

    @Override
    public void afterThrowing(Method method, Object[] args, Throwable error) {
        Object obj = args[0];
        if (obj instanceof ISocket) {
            ISocket socket = (ISocket) obj;
            String type = socket.get(Constant.TYPE);
            switch (type) {
                case Constant.CLIENT:
                    break;
                case Constant.SERVER:
                    Message reply = new Message(Constant.SRV_TEXT);
                    reply.add(Constant.TEXT, "请登录后操作");
                    socket.send(reply);
                    break;
            }
        }
    }
}
