package org.example.dto;

import java.time.LocalDate;

import org.example.dto.annotation.MaxAge;
import org.example.dto.annotation.MinAge;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDto(

                @NotBlank(message = "El username es obligatorio") @Size(min = 2, max = 30, message = "El username debe tener entre 2 y 30 caracteres") @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "El username solo puede contener letras mayusculas, minúsculas, números y guiones bajos") String username,

                @NotBlank(message = "El email es obligatorio") @Email(message = "El email debe tener un formato válido") String email,

                @NotBlank(message = "La contraseña es obligatoria") @Size(min = 6, max = 12, message = "La contraseña debe tener entre 6 y 12 caracteres") String password,

                @NotNull(message = "La fecha de nacimiento es obligatoria") @MinAge(value = 13, message = "La edad mínima es 13 años") @MaxAge(value = 99, message = "La edad máxima es 99 años") LocalDate birthDate) {
}
