module spring.start {
    requires spring.beans;
    requires spring.context;
    requires spring.test;
    requires junit;
    opens com.test.chapter01;
}