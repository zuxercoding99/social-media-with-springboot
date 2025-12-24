package org.example.dto;

import java.time.Instant;
import java.util.UUID;

import org.example.entity.Post;
import org.example.entity.Privacy;
import org.example.entity.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

        @JsonProperty(access = Access.READ_ONLY)
        private UUID id;

        @NotBlank
        @Size(max = 300)
        private String content;

        private Privacy privacy;

        @JsonProperty(access = Access.READ_ONLY)
        private PostAuthorDto author;

        @JsonProperty(access = Access.READ_ONLY)
        private FileMetadataDto file;

        @JsonProperty(access = Access.READ_ONLY)
        private Instant createdAt;

        @JsonProperty(access = Access.READ_ONLY)
        private long commentCount;

        @JsonProperty(access = Access.READ_ONLY)
        private long likeCount;

        @JsonProperty(access = Access.READ_ONLY)
        private boolean liked;

        public static PostDto from(Post entity,
                        long commentCount,
                        long likeCount,
                        boolean liked) {

                User u = entity.getUser();

                return PostDto.builder()
                                .id(entity.getId())
                                .content(entity.getContent())
                                .privacy(entity.getPrivacy())
                                .author(new PostAuthorDto(
                                                u.getId(),
                                                u.getUsername(),
                                                u.getDisplayName(),
                                                "/api/v1/avatars/" + u.getAvatarKey()))
                                .file(entity.getFileMetadata() != null
                                                ? FileMetadataDto.from(entity.getFileMetadata())
                                                : null)
                                .createdAt(entity.getCreatedAt())
                                .commentCount(commentCount)
                                .likeCount(likeCount)
                                .liked(liked)
                                .build();
        }

}
