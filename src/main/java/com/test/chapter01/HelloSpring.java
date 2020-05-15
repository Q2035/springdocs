package com.test.chapter01;

import com.test.chapter01.pojo.ContextConfig;
import com.test.chapter01.pojo.JDBCPro;
import com.test.chapter01.pojo.TestLifeCircle;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;


@ComponentScan("com.test")
public class HelloSpring {
    public static void main(String[] args) {
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:pojo.xml");
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(ContextConfig.class);
        JDBCPro bean = applicationContext.getBean(JDBCPro.class);
//        TestLifeCircle bean = applicationContext.getBean(TestLifeCircle.class);

        System.out.println(bean);
        Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(System.out::println);
    }
}