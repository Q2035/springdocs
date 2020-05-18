package com.test.chapter03.pojo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class CustomDateEditor extends PropertyEditorSupport {

    private DateFormat dateFormat;

    public CustomDateEditor(@Value("2020-01-01 09:21:20") String time) {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            Object value = dateFormat.parse(text);
            setValue(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAsText() {
        if (getValue() instanceof Date) {
            Date d = (Date) getValue();
            return dateFormat.format(d);
        }
        return super.getAsText();
    }

    @Override
    public String toString() {
        return "CustomDateEditor{" +
                "dateFormat=" + dateFormat +
                '}';
    }
}
