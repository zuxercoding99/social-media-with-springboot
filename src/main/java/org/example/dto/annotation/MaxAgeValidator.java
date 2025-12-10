package org.example.dto.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class MaxAgeValidator implements ConstraintValidator<MaxAge, LocalDate> {

    private int maxAge;

    @Override
    public void initialize(MaxAge constraintAnnotation) {
        this.maxAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null)
            return true; // O false si quieres que sea obligatorio

        LocalDate now = LocalDate.now();
        int age = Period.between(birthDate, now).getYears();
        return age <= maxAge;
    }
}
