package org.example.dto.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxAgeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxAge {
    String message() default "La edad debe ser menor o igual a {value} años";

    int value(); // Edad máxima permitida

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
