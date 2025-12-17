package org.example.dto;

import jakarta.validation.constraints.Size;

public record UpdateBioDto(

        @Size(max = 101, message = UpdateBioDto.MESSAGE) String bio

) {
    public static final String MESSAGE = "La biografía debe tener hasta 101 caracteres";
}