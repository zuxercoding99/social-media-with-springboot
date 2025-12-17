package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDisplayNameDto(

        @NotBlank(message = UpdateDisplayNameDto.MESSAGE) @Size(min = 2, max = 30, message = UpdateDisplayNameDto.MESSAGE) String displayName

) {
    public static final String MESSAGE = "El displayName debe tener entre 2 y 30 caracteres";
}