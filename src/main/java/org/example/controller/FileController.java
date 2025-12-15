package org.example.controller;

import org.example.dto.FileWithContentType;
import org.example.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {

        FileWithContentType fileWithType = fileService.getFileByFilename(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileWithType.getResource().getFilename() + "\"")
                .contentType(MediaType.parseMediaType(fileWithType.getContentType()))
                .body(fileWithType.getResource());
    }
}
