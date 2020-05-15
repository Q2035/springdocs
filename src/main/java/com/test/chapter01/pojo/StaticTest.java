package com.test.chapter01.pojo;

public class StaticTest {

    static {
        System.out.println("INIT");
    }

    private static int i = 1;

    public StaticTest(){
        System.out.println("StaticTest");
    }

    public static void hi(){
        System.out.println("HI");
    }
}

class Hi{
    public static void main(String[] args) {
        StaticTest.hi();
    }
}
