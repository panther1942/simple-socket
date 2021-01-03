package cn.erika.aop;

import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.bean.Advise;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;

import java.lang.reflect.Method;

@Component
public class ExchangeExceptionHandler implements Advise {
    @Override
    public void before(Method method, Object[] args) throws Throwable {

    }

    @Override
    public void afterReturning(Method method, Object[] args, Object result) {

    }

    @Override
    public void afterThrowing(Method method, Object[] args, Throwable error) {
        Object obj = args[0];
        if (obj instanceof ISocket) {
            ISocket socket = (ISocket) obj;
            Message reply = new Message(Constant.SRV_EXCHANGE_RESULT);
            reply.add(Constant.RESULT, false);
            reply.add(Constant.TEXT, error.getMessage());
            socket.send(reply);
            // 协商失败则告知后断开连接
            socket.close();
        }
    }
}
