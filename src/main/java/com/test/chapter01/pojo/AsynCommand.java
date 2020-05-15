package com.test.chapter01.pojo;

import org.springframework.stereotype.Component;

@Component
public class AsynCommand extends Command {
    @Override
    public Command create() {
        return null;
    }
}
