package org.example.dto;

import java.util.UUID;

public record CommentAuthorDto(
        UUID id,
        String username,
        String displayName,
        String avatarUrl) {
}
