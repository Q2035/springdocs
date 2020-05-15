package com.test.chapter01.test;

public class TestStatic {

    static {
        System.out.println("static");
    }

    private TestStatic(){
        System.out.println("private com.test.chapter01.test.TestStatic");
    }

    static class Hello extends TestStatic{
        public Hello(){
            new TestStatic();
        }
    }
}

class Hi{
    public static void main(String[] args) {
        new TestStatic.Hello();
        new TestStatic.Hello();
    }
}
