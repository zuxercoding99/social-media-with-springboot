package org.example.controller;

import org.example.dto.PostCreateRequest;
import org.example.dto.PostDto;
import org.example.entity.Privacy;
import org.example.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<PostDto> createPost(
            @Valid @ModelAttribute PostCreateRequest request,
            @RequestParam(required = false) MultipartFile file) throws IOException {

        PostDto dto = postService.createPost(request.getContent(),
                request.getPrivacy() != null ? request.getPrivacy() : Privacy.PUBLIC, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);

    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable UUID id) {
        PostDto dto = postService.getPostById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostDto>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getFeed(page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<PostDto>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getMyPosts(page, size));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Page<PostDto>> getPostsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getPostsByUsername(username, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable UUID id) {
        postService.toggleLike(id);
        return ResponseEntity.ok().build();
    }

}
