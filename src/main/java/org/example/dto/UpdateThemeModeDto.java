package org.example.dto;

import jakarta.validation.constraints.NotNull;
import org.example.entity.ThemeMode;

public record UpdateThemeModeDto(
        @NotNull ThemeMode themeMode) {
}
