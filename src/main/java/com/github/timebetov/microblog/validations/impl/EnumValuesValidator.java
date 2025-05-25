package com.github.timebetov.microblog.validations.impl;

import com.github.timebetov.microblog.validations.EnumValues;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValuesValidator implements ConstraintValidator<EnumValues, CharSequence> {

    private Set<String> acceptedValues;
    private String acceptedValuesString;

    @Override
    public void initialize(EnumValues constraint) {

        Class<? extends Enum<?>> enumClass = constraint.enumClass();
        Enum<?>[] enumConstants = enumClass.getEnumConstants();

        acceptedValues = Arrays.stream(enumConstants)
                .map(e -> e.name().toUpperCase())
                .collect(Collectors.toSet());

        acceptedValuesString = String.join(", ", acceptedValues);
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) return true;

        boolean isValid = acceptedValues.contains(value.toString().trim().toUpperCase());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Value must be one of: " + acceptedValuesString
            ).addConstraintViolation();
        }

        return isValid;
    }
}
