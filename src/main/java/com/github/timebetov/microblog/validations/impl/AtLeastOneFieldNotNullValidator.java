package com.github.timebetov.microblog.validations.impl;

import com.github.timebetov.microblog.validations.AtLeasOneFieldPresent;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class AtLeastOneFieldNotNullValidator implements ConstraintValidator<AtLeasOneFieldPresent, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {

        if (obj == null) return false;

        for (Field field : obj.getClass().getDeclaredFields()) {

            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                // Ignoring
            }
        }
        return false;
    }
}
