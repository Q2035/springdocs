package com.test.chapter03.pojo;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class CustomerValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Customer.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors,"name","name.empty");
        Customer customer = (Customer) target;
        if (customer.getAge() < 0) {
            errors.rejectValue("age","negative age!");
        }
    }
}
