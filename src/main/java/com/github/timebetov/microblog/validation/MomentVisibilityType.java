package com.github.timebetov.microblog.validation;

import com.github.timebetov.microblog.validation.impl.MomentVisibilityTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = MomentVisibilityTypeValidator.class)
public @interface MomentVisibilityType {

    Class<? extends Enum<?>> enumClass();
    String message() default "Value must be one of the following: {enumValues}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
