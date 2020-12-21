package cn.erika.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class DemoAdvice {

    @Pointcut(value = "execution(* cn.erika.service.IDemoService.sum(int,int))")
    public void aspect() {
    }

    @Before(value = "aspect() && args(a,b)", argNames = "joinPoint,a,b")
    public void before(JoinPoint joinPoint, int a, int b) {
        System.out.println("before run: " + joinPoint.getSignature().getName());
        System.out.println("a: " + a);
        System.out.println("b: " + b);
    }
}
