package org.example.dto.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UsernameConstraint {
    String message() default "Username must be 3-12 characters, only letters, numbers and underscores.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}