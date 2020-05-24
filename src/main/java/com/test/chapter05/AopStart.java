package com.test.chapter05;

import com.test.chapter05.aop.AopConfig;
import com.test.chapter05.aop.NotVeryUseful;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AopStart {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AopConfig.class);
        NotVeryUseful bean = applicationContext.getBean(NotVeryUseful.class);
        bean.hi();
        System.out.println(bean);
    }
}
