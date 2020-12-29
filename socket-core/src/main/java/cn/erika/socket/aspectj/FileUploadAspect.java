package cn.erika.socket.aspectj;

import cn.erika.config.Constant;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.handler.bio.FileSender;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class FileUploadAspect {

    @Pointcut("execution(* cn.erika.socket.handler.bio.FileSender.onMessage(..))")
    public void pointcut() {
    }

    @AfterReturning(value = "pointcut() && args(socket,message)", argNames = "joinPoint,socket,message")
    public void afterReturning(JoinPoint joinPoint, ISocket socket, Message message) {
        if (Constant.SRV_EXCHANGE_TOKEN.equals(message.get(Constant.SERVICE_NAME))) {
            boolean result = message.get(Constant.RESULT);
            if (result) {
                FileSender fileSender = (FileSender) joinPoint.getTarget();
                fileSender.upload();
            }
        }
    }
}
