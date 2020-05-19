package com.test.chapter04.spel;

import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class TestSpEL {

    @Test
    public void startSpEL(){
        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression("'hello '.concat('world').bytes.length");
        System.out.println(expression.getValue());
    }
}
