package org.example.service;

import java.io.IOException;
import java.util.UUID;

import org.apache.tika.Tika;
import org.example.dto.FileWithContentType;
import org.example.entity.FileMetadata;
import org.example.entity.Friend;
import org.example.entity.Post;
import org.example.entity.Privacy;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.BadRequestException;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.repository.FileMetadataRepository;
import org.example.repository.FriendRepository;
import org.example.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageService storageService;
    private final FileMetadataRepository fileRepo;
    private final UserRepository userRepo;
    private final FriendRepository friendRepo;
    private final AuthService authAuthService;

    /**
     * Sube el archivo y crea FileMetadata YA asociado al Post.
     */
    public FileMetadata uploadForPost(MultipartFile file, User owner, Post post) throws IOException {
        // 1) Validar archivo
        String contentType = validateImage(file);

        // 2) Generar filename
        String filename = UUID.randomUUID() + getExtension(contentType);

        // 3) Guardar físicamente (usamos un keyPrefix CONSISTENTE)
        String keyPrefix = "posts/" + owner.getUsername();
        String url = storageService.save(file, keyPrefix, filename);

        // 4) Crear metadata con relación obligatoria al post
        FileMetadata metadata = new FileMetadata();
        metadata.setKey(filename);
        metadata.setOriginalName(file.getOriginalFilename());
        metadata.setContentType(contentType);
        metadata.setSize(file.getSize());
        metadata.setUrl(url);
        metadata.setOwner(owner);
        metadata.setPost(post); // <- obligatorio

        return fileRepo.save(metadata);
    }

    /**
     * Lee un archivo por filename validando permisos en base a la privacy del Post.
     */
    public FileWithContentType getFileByFilename(String filename) {

        String currentUsername = authAuthService.getCurrentUsernameOptional().orElse(null);

        FileMetadata meta = fileRepo.findByKey(filename)
                .orElseThrow(() -> new NotFoundException("Archivo no encontrado"));

        Post post = meta.getPost();
        User owner = meta.getOwner();

        // Permisos por privacy del Post
        if (post.getPrivacy() == Privacy.PUBLIC) {
            // ok
        } else {
            if (currentUsername == null) {
                throw new AccessDeniedException("Acceso denegado");
            }
            if (owner.getUsername().equals(currentUsername)) {
                // dueño siempre puede
            } else if (post.getPrivacy() == Privacy.PRIVATE) {
                throw new AccessDeniedException("Acceso denegado");
            } else if (post.getPrivacy() == Privacy.FRIENDS) {
                User current = userRepo.findByUsername(currentUsername)
                        .orElseThrow(() -> new AccessDeniedException("Acceso denegado"));
                var rel = friendRepo.findByRequesterAndReceiverOrRequesterAndReceiver(
                        owner, current, current, owner);
                if (rel.isEmpty() || rel.get().getStatus() != Friend.FriendStatus.ACCEPTED) {
                    throw new AccessDeniedException("Acceso denegado");
                }
            } else {
                throw new AccessDeniedException("Acceso denegado");
            }
        }

        try {
            String keyPrefix = "posts/" + owner.getUsername();
            Resource resource = storageService.loadAsResource(keyPrefix, meta.getKey());
            return new FileWithContentType(resource, meta.getContentType());
        } catch (Exception e) {
            throw new BadRequestException("Error al cargar archivo");
        }
    }

    // --- Auxiliares ---
    private String validateImage(MultipartFile file) throws IOException {
        if (file.isEmpty())
            throw new BadRequestException("El archivo está vacío");
        if (file.getSize() > 5 * 1024 * 1024)
            throw new BadRequestException("Archivo demasiado grande");

        Tika tika = new Tika();
        String detectedType;
        try (var is = file.getInputStream()) {
            detectedType = tika.detect(is);
        }
        if (!(detectedType.equals("image/png") || detectedType.equals("image/jpeg"))) {
            throw new BadRequestException("Solo se permiten imágenes PNG o JPG");
        }
        return detectedType;
    }

    private String getExtension(String contentType) {
        return contentType.equals("image/png") ? ".png" : ".jpg";
    }
}
