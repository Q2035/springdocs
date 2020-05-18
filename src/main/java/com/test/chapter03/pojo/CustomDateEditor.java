package com.test.chapter03.pojo;

import java.text.DateFormat;
import java.beans.PropertyEditorSupport;

public class CustomDateEditor extends PropertyEditorSupport {

    private DateFormat dateFormat;
    private boolean allowEmpty;
    private int exactDateLength;

    public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty) {
        this.dateFormat = dateFormat;    //时间格式转换器
        this.allowEmpty = allowEmpty;    //设置是否允许时间为空
        this.exactDateLength = -1;       //设置精确对象长度，-1为不限制
    }

    public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty, int exactDateLength) {
        this.dateFormat = dateFormat;
        this.allowEmpty = allowEmpty;
        this.exactDateLength = exactDateLength;
    }
}
