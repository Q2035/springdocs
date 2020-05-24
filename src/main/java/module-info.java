module spring_start {

    requires spring.beans;
    requires spring.context;
    requires spring.test;
    requires spring.expression;
    requires junit;
    requires spring.core;
    requires spring.web;

    requires org.aspectj.weaver;
//    。。。
//    java.beans居然在这个模块
    requires java.desktop;

    exports com.test.chapter04;

    opens com.test.chapter01;
    opens com.test.chapter02.pojo;
    opens com.test.chapter02.config;
    opens com.test.chapter03.config;
    opens com.test.chapter05.aop;

    exports com.test.chapter02.config;
    exports com.test.chapter02.pojo;
    exports com.test.chapter03.pojo;
    exports com.test.chapter04.spel;
    exports com.test.chapter05.pojo;
}