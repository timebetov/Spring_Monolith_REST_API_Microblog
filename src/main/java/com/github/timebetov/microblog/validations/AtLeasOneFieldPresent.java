package com.github.timebetov.microblog.validations;

import com.github.timebetov.microblog.validations.impl.AtLeastOneFieldNotNullValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldNotNullValidator.class)
@Documented
public @interface AtLeasOneFieldPresent {

    String message() default "At least one field must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
