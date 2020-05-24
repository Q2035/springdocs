package com.test.chapter05.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NotVeryUseful {
    public void hi(){
        System.out.println("HI");
    }
}
