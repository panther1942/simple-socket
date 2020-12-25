package cn.erika.socket.aop;

import cn.erika.context.annotation.Component;
import cn.erika.context.bean.Advise;
import cn.erika.util.string.ConsoleUtils;

import java.lang.reflect.Method;

@Component
public class SocketReceiveAspect implements Advise {
    @Override
    public void before(Method method, Object[] args) {
        System.out.println(ConsoleUtils.drawLineWithTitle("接收消息前", "-", 100));
    }

    @Override
    public void afterReturning(Method method, Object[] args, Object result) {

    }

    @Override
    public void afterThrowing(Method method, Object[] args, Throwable error) {

    }
}
