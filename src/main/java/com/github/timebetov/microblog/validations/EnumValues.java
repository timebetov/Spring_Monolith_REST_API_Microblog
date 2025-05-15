package com.github.timebetov.microblog.validations;

import com.github.timebetov.microblog.validations.impl.EnumValuesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValuesValidator.class)
public @interface EnumValues {

    Class<? extends Enum<?>> enumClass();
    String message() default "Value must be one of the following: {enumValues}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
