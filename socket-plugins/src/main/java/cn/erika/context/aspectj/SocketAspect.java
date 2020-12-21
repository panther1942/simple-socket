package cn.erika.context.aspectj;

import cn.erika.context.Application;
import cn.erika.socket.core.component.DataInfo;
import cn.erika.socket.plugins.SocketPlugin;
import cn.erika.socket.core.Socket;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SocketAspect {

    @Pointcut("execution(* cn.erika.socket.core.Socket.receive(cn.erika.socket.core.component.DataInfo))")
    public void pointcutSocketReceive() {
    }

    @Pointcut("execution(* cn.erika.socket.core.Socket.send(cn.erika.socket.core.component.DataInfo))")
    public void pointcutSocketSend() {
    }


    @Before(value = "pointcutSocketReceive() && args(dataInfo)", argNames = "joinPoint,dataInfo")
    public void beforeSocketReceive(JoinPoint joinPoint, DataInfo dataInfo) {
        Socket socket = (Socket) joinPoint.getTarget();

    }

    @Before(value = "pointcutSocketSend() && args(dataInfo)", argNames = "joinPoint,dataInfo")
    public void beforeSocketSend(JoinPoint joinPoint, DataInfo dataInfo) {
        Socket socket = (Socket) joinPoint.getTarget();

    }
}
