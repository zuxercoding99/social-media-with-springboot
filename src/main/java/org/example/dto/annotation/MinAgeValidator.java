package org.example.dto.annotation;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {
    private int minAge;

    @Override
    public void initialize(MinAge constraintAnnotation) {
        this.minAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null)
            return true; // @NotNull se encarga
        LocalDate minBirth = LocalDate.now().minusYears(minAge);
        return birthDate.isBefore(minBirth) || birthDate.isEqual(minBirth);
    }
}
