package org.example.controller;

import java.util.List;
import java.util.UUID;

import org.example.dto.CommentCreateRequest;
import org.example.dto.CommentDto;
import org.example.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @PathVariable UUID postId,
            @RequestBody @Valid CommentCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(postId, dto.getContent()));
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getCommentsForPost(postId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID postId,
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
