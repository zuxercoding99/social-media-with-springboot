package org.example.dto;

import java.util.UUID;

public record FriendDto(
        UUID id,
        String username,
        String displayName) {
}
