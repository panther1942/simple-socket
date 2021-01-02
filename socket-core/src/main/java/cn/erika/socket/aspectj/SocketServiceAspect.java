package cn.erika.socket.aspectj;

import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.string.ConsoleUtils;
import org.aspectj.lang.JoinPoint;

//@Aspect
public class SocketServiceAspect {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    // @Pointcut("execution(* cn.erika.socket.services.ISocketService.*(..))")
    public void pointcut() {
    }

    // @Before("pointcut()")
    public void beforeExecute(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.debug((ConsoleUtils.drawLineWithTitle(className + "." + methodName, "-", 100)));
    }
}
