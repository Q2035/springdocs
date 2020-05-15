package com.test.chapter01;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ClassA.class,ClassB.class})
public class SpringTest {
    @Autowired
    private ClassA classA;

    @Test
    public void simpleTest() {
        for (int i = 0; i < 3; i++) {
            classA.printClass();
        }
    }
}

@Component
class ClassA {

//    @Autowired
//    private ClassB classB;

    public void printClass() {
        System.out.println("This is Class A: " + this);
        getClassB().printClass();
    }


    /**
     * 让Spring重写方法
     * @return
     */
    @Lookup
    public ClassB getClassB(){
        return null;
    }
}

@Component
@Scope(value = SCOPE_PROTOTYPE)
class ClassB {
    public void printClass() {
        System.out.println("This is Class B: " + this);
    }

}