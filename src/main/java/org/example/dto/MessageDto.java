package org.example.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
                Long id,
                Long friendId,
                UUID senderId,
                String senderUsername,
                String senderDisplayName,
                String senderAvatarUrl,
                String content,
                Instant sentAt) {
}
