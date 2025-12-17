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
        Files.createDirectories(rootPath.resolve("avatars"));
    }

    @Override
    public void save(MultipartFile file, String keyPrefix, String filename) throws IOException {
        Path dir = rootPath.resolve(keyPrefix);
        Files.createDirectories(dir);

        try (var is = file.getInputStream()) {
            Files.copy(is, dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public Resource loadAsResource(String keyPrefix, String filename)
            throws MalformedURLException {

        Path path = rootPath.resolve(keyPrefix).resolve(filename);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("Archivo no encontrado");
        }

        return resource;
    }

    @Override
    public void delete(String keyPrefix, String filename) throws IOException {
        Files.deleteIfExists(rootPath.resolve(keyPrefix).resolve(filename));
    }
}
