package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;

@Configuration
public class StorageInitializer {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    @Value("${app.storage.local.root:uploads}")
    private String rootDir;

    @PostConstruct
    public void init() throws IOException {
        Path avatarsDir = Path.of(rootDir, "avatars");
        Files.createDirectories(avatarsDir);

        copyIfNotExists("img/default.png", avatarsDir.resolve("default.png"));
        copyIfNotExists("img/owner.jpg", avatarsDir.resolve("owner.jpg"));
    }

    private void copyIfNotExists(String classpathFile, Path target)
            throws IOException {
        log.info("Entrando a copyIfNotExists");
        if (Files.exists(target))
            return;

        ClassPathResource resource = new ClassPathResource(classpathFile);

        if (!resource.exists()) {
            log.info("No existe resource");

            throw new IllegalStateException(
                    "NO EXISTE EN CLASSPATH: " + classpathFile);
        }

        log.info("Existe resource");
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
