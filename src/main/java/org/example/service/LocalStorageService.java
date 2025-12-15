package org.example.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.example.exception.customs.httpstatus.NotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootPath = Paths.get("uploads");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(rootPath);
    }

    @Override
    public String save(MultipartFile file, String keyPrefix, String filename) throws IOException {
        Path dir = rootPath.resolve(keyPrefix);
        Files.createDirectories(dir);

        Path filePath = dir.resolve(filename);

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/api/v1/files/" + filename; // URL para acceder al archivo
    }

    @Override
    public Resource loadAsResource(String keyPrefix, String filename) throws MalformedURLException {
        Path filePath = rootPath.resolve(keyPrefix).resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("Archivo no encontrado: " + filename);
        }
        return resource;
    }

    @Override
    public void delete(String keyPrefix, String filename) throws IOException {
        Path filePath = rootPath.resolve(keyPrefix).resolve(filename);
        Files.deleteIfExists(filePath);
    }
}
