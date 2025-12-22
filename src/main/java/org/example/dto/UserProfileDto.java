package org.example.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String username,
        String displayName,
        String avatarUrl,
        String bio,
        Instant createdAt,
        long postCount,
        long friendCount,
        boolean isFriend,
        boolean sentRequest,
        boolean receivedRequest) {
}
