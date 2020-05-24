package com.test.chapter05.pojo;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

    @Before("execution(* com.test.chapter05.aop.NotVeryUseful.hi())")
    public void beforeExe(){
        System.out.println("Before exe");
    }

    @After("execution(* com.test.chapter05.aop.NotVeryUseful.hi())")
    public void afterExe(){
        System.out.println("After exe");
    }
}
