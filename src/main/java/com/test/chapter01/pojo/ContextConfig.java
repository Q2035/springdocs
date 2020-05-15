package com.test.chapter01.pojo;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@Configurable
@ComponentScan("com.test")
public class ContextConfig {

    @Bean
    private BeanOne beanOne(){
        return new BeanOne(beanTwo());
    }

    @Bean
    private BeanTwo beanTwo(){
        return new BeanTwo();
    }

    @Bean
    private TestLifeCircle testLifeCircle(){
        return new TestLifeCircle();
    }

}

class BeanOne{
    private BeanTwo beanTwo;

    public BeanOne(BeanTwo beanTwo) {
        this.beanTwo = beanTwo;
    }

    public BeanOne() {
    }
}

class BeanTwo{

}