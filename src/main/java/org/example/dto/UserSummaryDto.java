package org.example.dto;

import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String username,
        String displayName,
        String avatarUrl) {
}
