package cn.erika.socket.aspectj;

import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.string.ConsoleUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SocketServiceAspect {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* cn.erika.socket.services.ISocketService.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void beforeExecute(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        System.out.println(ConsoleUtils.drawLineWithTitle(
                className + "." + methodName, "-", 100));
    }
}
