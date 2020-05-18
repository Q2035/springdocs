package com.test.chapter03;

import com.test.chapter03.config.ValidConfig;
import com.test.chapter03.pojo.*;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

public class ValidSpring {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(ValidConfig.class);

        System.out.println(applicationContext.getBean(CustomDateEditor.class));

//        testBeanWrapper();

        Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(System.out::println);
    }

    private static void testBeanWrapper() {
        BeanWrapper company = new BeanWrapperImpl(new Company());
// setting the company name..
        company.setPropertyValue("name", "Some Company Inc.");
// ... can also be done like this:
        PropertyValue value = new PropertyValue("name", "Some Company Inc.");
        company.setPropertyValue(value);

// ok, let's create the director and tie it to the company:
        BeanWrapper jim = new BeanWrapperImpl(new Employee());
        jim.setPropertyValue("name", "Jim Stravinsky");
        jim.setPropertyValue("salary",1232);

        company.setPropertyValue("managingDirector", jim.getWrappedInstance());

// retrieving the salary of the managingDirector through the company
        Float salary = (Float) company.getPropertyValue("managingDirector.salary");
        System.out.println(salary);
    }
}
