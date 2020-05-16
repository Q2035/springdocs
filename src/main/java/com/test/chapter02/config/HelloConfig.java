package com.test.chapter02.config;

import com.test.chapter02.pojo.User;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@Configurable
@ComponentScan("com.test.chapter02")
public class HelloConfig {

    @Bean
    public User user(){
        return new User();
    }

}
