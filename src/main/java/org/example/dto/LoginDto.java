package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDto(
        @NotBlank(message = "Email must not be blank") @Email(message = "Email must be a valid format") String email,

        @NotBlank(message = "Password must not be blank") @Size(min = 6, max = 12, message = "Password must be between 6 and 12 characters") String password) {
}
