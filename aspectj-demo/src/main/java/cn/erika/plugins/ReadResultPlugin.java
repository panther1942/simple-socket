package cn.erika.plugins;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ReadResultPlugin implements Plugin {

    @AfterReturning(value = "execution(* cn.erika.service.impl.*.*(..))", returning = "result")
    public void readResult(JoinPoint joinPoint, Object result) {
        System.out.println(result);
    }
}
