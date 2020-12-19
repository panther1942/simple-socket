package cn.erika.plugins;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class ReadParamsPlugin implements Plugin {

    @Before("execution(* cn.erika.service.impl.*.*(..))")
    public void readParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object obj : args) {
            System.out.println(obj);
        }
    }
}
