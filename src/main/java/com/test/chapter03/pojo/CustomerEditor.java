package com.test.chapter03.pojo;

import org.springframework.stereotype.Component;

import java.beans.PropertyEditorSupport;

@Component
public class CustomerEditor extends PropertyEditorSupport {


    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(new Customer(text.toUpperCase(), 19));
    }

}
