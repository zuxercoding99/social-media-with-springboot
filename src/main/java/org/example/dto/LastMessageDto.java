package org.example.dto;

import java.time.Instant;
import java.util.UUID;

public record LastMessageDto(
        UUID senderId,
        String content,
        Instant sentAt) {
}
