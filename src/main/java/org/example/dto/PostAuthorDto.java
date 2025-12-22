package org.example.dto;

import java.util.UUID;

public record PostAuthorDto(
                UUID id,
                String username,
                String displayName,
                String avatarUrl) {
}
