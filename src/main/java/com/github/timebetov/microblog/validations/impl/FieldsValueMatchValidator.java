package com.github.timebetov.microblog.validations.impl;

import com.github.timebetov.microblog.validations.FieldsValueMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;

public class FieldsValueMatchValidator implements ConstraintValidator<FieldsValueMatch, Object> {

    private String field;
    private String fieldMatch;

    @Override
    public void initialize(FieldsValueMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.fieldMatch = constraintAnnotation.fieldMatch();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext context) {

        Object fieldValue = new BeanWrapperImpl(o)
                .getPropertyValue(field);
        Object fieldMatchValue = new BeanWrapperImpl(o)
                .getPropertyValue(fieldMatch);

        return Objects.equals(fieldValue, fieldMatchValue);
    }
}
