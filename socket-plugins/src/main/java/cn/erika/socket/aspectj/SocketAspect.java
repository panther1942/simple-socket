package cn.erika.socket.aspectj;

import cn.erika.config.Constant;
import cn.erika.socket.core.component.Message;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SocketAspect {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* cn.erika.socket.core.BaseSocket.receive(cn.erika.socket.core.component.Message))")
    public void receive() {
    }

    @Pointcut("execution(* cn.erika.socket.core.BaseSocket.send(cn.erika.socket.core.component.Message))")
    public void sender() {
    }

    @Before(value = "receive() && args(message)", argNames = "joinPoint,message")
    public void beforeReceive(JoinPoint joinPoint, Message message) {
        log.debug("BeforeReceive: " + message.get(Constant.SERVICE_NAME));
    }

    @Before(value = "sender() && args(message)", argNames = "joinPoint,message")
    public void beforeSend(JoinPoint joinPoint, Message message) {
        log.debug("BeforeSend: " + message.get(Constant.SERVICE_NAME));
    }

}
