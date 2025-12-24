package org.example.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateBannerColorDto(
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Ingrese un valor valido") String bannerColor) {
}
