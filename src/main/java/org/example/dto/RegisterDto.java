package org.example.dto;

import java.time.LocalDate;

import org.example.dto.annotation.MaxAge;
import org.example.dto.annotation.MinAge;
import org.example.dto.annotation.UsernameConstraint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDto(
                @NotBlank(message = "Username must not be blank") @UsernameConstraint String username,

                @NotBlank(message = "Email must not be blank") @Email(message = "Email must be a valid format") String email,

                @NotBlank(message = "Password must not be blank") @Size(min = 6, max = 12, message = "Password must be between 6 and 12 characters") String password,

                @NotNull(message = "Birth date is required") @MinAge(value = 13, message = "Minimum allowed age is 13") @MaxAge(value = 99, message = "Maximum allowed age is 99") LocalDate birthDate) {
}
