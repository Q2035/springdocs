package com.test.chapter01.pojo;

import org.springframework.beans.factory.annotation.Lookup;

public abstract class Command {

    @Lookup
    public abstract Command create();
}
