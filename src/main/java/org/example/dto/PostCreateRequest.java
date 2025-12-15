package org.example.dto;

import org.example.entity.Privacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(max = 300, message = "El contenido no puede superar los 300 caracteres")
    private String content;

    // Puede ser null si no se envía, y en el service lo defaultás a PUBLIC
    private Privacy privacy;
}
