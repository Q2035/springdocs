package com.test.chapter03.config;

import com.test.chapter03.pojo.Customer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.test.chapter03")
public class ValidConfig {

    @Bean
    public Customer customer(){
        return new Customer("validation",-1);
    }

}
