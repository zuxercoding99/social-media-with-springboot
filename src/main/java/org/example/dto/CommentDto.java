package org.example.dto;

import java.time.Instant;
import java.util.UUID;

import org.example.entity.Comment;
import org.example.entity.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    @JsonProperty(access = Access.READ_ONLY)
    private Long id;

    private String content;

    @JsonProperty(access = Access.READ_ONLY)
    private CommentAuthorDto author;

    @JsonProperty(access = Access.READ_ONLY)
    private Instant createdAt;

    public static CommentDto from(Comment comment) {
        User u = comment.getUser();
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(new CommentAuthorDto(u.getId(), u.getUsername(), u.getDisplayName(),
                        "/api/v1/avatars/" + u.getAvatarKey()))
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
