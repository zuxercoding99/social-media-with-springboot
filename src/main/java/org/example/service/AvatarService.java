package org.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.example.exception.customs.httpstatus.BadRequestException;
import org.example.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private static final String AVATAR_DIR = "avatars";
    private static final String DEFAULT_AVATAR = "default.png";

    private final StorageService storageService;
    private final AuthService authService;

    @Transactional
    public void uploadAvatar(MultipartFile file) throws IOException {

        User user = authService.getCurrentUser();

        String contentType = validateImage(file);
        String newFilename = UUID.randomUUID() + getExtension(contentType);

        // borrar avatar anterior si NO es default
        if (!DEFAULT_AVATAR.equals(user.getAvatarKey())) {
            storageService.delete(AVATAR_DIR, user.getAvatarKey());
        }

        storageService.save(file, AVATAR_DIR, newFilename);

        user.setAvatarKey(newFilename);
    }

    @Transactional(readOnly = true)
    public Resource loadAvatar(String filename) throws Exception {
        return storageService.loadAsResource(AVATAR_DIR, filename);
    }

    // ---- helpers ----

    private String validateImage(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new BadRequestException("Archivo vacío");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Máximo 5MB");
        }

        Tika tika = new Tika();
        try (var is = file.getInputStream()) {
            String type = tika.detect(is);
            if (!type.equals("image/png") && !type.equals("image/jpeg")) {
                throw new BadRequestException("Solo PNG o JPG");
            }
            return type;
        }
    }

    private String getExtension(String contentType) {
        return contentType.equals("image/png") ? ".png" : ".jpg";
    }
}
