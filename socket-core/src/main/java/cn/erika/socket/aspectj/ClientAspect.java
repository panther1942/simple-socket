package cn.erika.socket.aspectj;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.handler.IClient;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ClientAspect {

    @Pointcut("execution(* cn.erika.socket.handler.bio.BIOClient.connect(..))")
    public void bioClient() {
    }

    @Pointcut("execution(* cn.erika.socket.handler.nio.NIOClient.finishConnect(..))")
    public void nioClient(){
    }

    @AfterReturning(value = "bioClient() || nioClient()")
    public void afterReturning(JoinPoint joinPoint) {
        IClient client = (IClient) joinPoint.getTarget();
        try {
            client.execute(Constant.SRV_EXCHANGE_KEY, null);
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }
}
