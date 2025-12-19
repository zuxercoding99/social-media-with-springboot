package org.example.dto;

import java.util.UUID;

public record ChatPreviewDto(
        Long friendId,
        UUID otherUserId,
        String otherUsername,
        String otherDisplayName,
        String otherAvatarUrl,
        LastMessageDto lastMessage,
        int unreadCount // 0 por ahora
) {
}
