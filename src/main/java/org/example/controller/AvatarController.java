package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.AvatarService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @PostMapping
    public ResponseEntity<Void> uploadAvatar(
            @RequestParam("file") MultipartFile file) throws IOException {

        avatarService.uploadAvatar(file);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename)
            throws Exception {

        Resource resource = avatarService.loadAvatar(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\"")
                .contentType(resolveContentType(filename))
                .body(resource);
    }

    private MediaType resolveContentType(String filename) {
        if (filename.endsWith(".png"))
            return MediaType.IMAGE_PNG;
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            return MediaType.IMAGE_JPEG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
