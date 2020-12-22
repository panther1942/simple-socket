package cn.erika.socket.aspectj;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.Client;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ClientAspect {

    @Pointcut("execution(* cn.erika.socket.handler.Client.connect(..))")
    public void pointcut() {
    }

    @AfterReturning(value = "pointcut()")
    public void afterReturning(JoinPoint joinPoint) {
        Client client = (Client) joinPoint.getTarget();
        try {
            client.execute(Constant.SRV_EXCHANGE_KEY, null);
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }
}
