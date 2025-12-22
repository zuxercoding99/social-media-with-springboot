package org.example.dto;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestDto(
                UUID requesterId,
                String requesterUsername,
                String requesterDisplayName,
                String avatarUrl,
                Instant createdAt) {
}
