package cn.erika.web.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class Demo2Advice {

    @Pointcut("execution(* cn.erika.web.service.impl.Demo2ServiceImpl.*(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        System.out.println("before run: "+joinPoint.getSignature().getName());
    }
}
