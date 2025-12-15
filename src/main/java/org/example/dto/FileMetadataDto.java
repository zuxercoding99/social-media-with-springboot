package org.example.dto;

import java.time.Instant;

import org.example.entity.FileMetadata;

public record FileMetadataDto(
        Long id,
        String originalName,
        String contentType,
        long size,
        String url,
        String ownerUsername,
        Instant createdAt) {
    public static FileMetadataDto from(FileMetadata entity) {
        return new FileMetadataDto(
                entity.getId(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSize(),
                entity.getUrl(),
                entity.getOwner().getUsername(),
                entity.getCreatedAt());
    }
}
