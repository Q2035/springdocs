package com.test.chapter01.pojo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

/**
 * 常规的LifeCycle接口只是在容器上下文显式的调用start()/stop()方法时，才会去回调LifeCycle的实现类的start stop方法逻辑。并不意味着在上下文刷新时自动启动。
 */
public class TestLifeCircle implements Lifecycle, InitializingBean {

    @Override
    public void start() {
        System.out.println("HI");
    }

    @Override
    public void stop() {
        System.out.println("Stop");
    }

    @Override
    public boolean isRunning() {
        return false;
    }


    public void afterPropertiesSet() throws Exception {
        System.out.println("HIHHIHIHIH");
    }
}
