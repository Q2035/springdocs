package com.test.chapter02;

import com.test.chapter02.config.HelloConfig;
import com.test.chapter02.pojo.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class HelloSpring {
    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(HelloConfig.class);
        applicationContext.refresh();

        Resource resource = applicationContext.getResource("user.properties");
        System.out.println(resource.contentLength());
        System.out.println(resource.getFilename());
        System.out.println(resource.isOpen());

        User bean = applicationContext.getBean(User.class);
        System.out.println(bean);
    }
}
