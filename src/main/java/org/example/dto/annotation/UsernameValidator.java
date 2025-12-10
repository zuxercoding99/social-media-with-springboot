package org.example.dto.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<UsernameConstraint, String> {
    private static final String USERNAME_PATTERN = "^(?!.*__)[a-zA-Z0-9](?:[a-zA-Z0-9_]{1,10}[a-zA-Z0-9])?$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null)
            return true;
        return value.matches(USERNAME_PATTERN);
    }
}
