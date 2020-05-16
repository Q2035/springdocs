module spring_start {
    requires spring.beans;
    requires spring.context;
    requires spring.test;
    requires junit;
    requires spring.core;
    opens com.test.chapter01;
    opens com.test.chapter02.pojo;
    opens com.test.chapter02.config;
    exports com.test.chapter02.config;
    exports com.test.chapter02.pojo;
}