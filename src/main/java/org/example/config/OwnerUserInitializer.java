package org.example.config;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import org.example.entity.Post;
import org.example.entity.Privacy;
import org.example.entity.Role;
import org.example.entity.ThemeMode;
import org.example.entity.User;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class OwnerUserInitializer {

    @Bean
    public CommandLineRunner initOwnerUser(UserRepository userRepo,
            PasswordEncoder encoder,
            RoleRepository roleRepo, @Value("${owner.password}") String rawPassword) {
        return args -> {
            String username = "zk99";

            if (!userRepo.existsByUsername(username)) {
                // Obtener o crear el rol
                Role role = roleRepo.findByName("ROLE_USER")
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setName("ROLE_USER");
                            return roleRepo.save(newRole);
                        });

                // Crear usuario
                User owner = new User();
                owner.setUsername(username);
                owner.setPassword(encoder.encode(rawPassword));
                owner.setBio("""
                        Programador Backend Java / Spring Boot

                        Looking for a job
                                                """);
                owner.setAvatarKey("owner.jpg");
                owner.setEnabled(true);
                owner.setThemeMode(ThemeMode.DARK);
                owner.setBannerColor("#d90d0d");
                owner.setBirthDate(LocalDate.of(1999, 1, 12));

                owner.setDisplayName("Z決 ✨");
                owner.setEmail("zk@example.com");
                owner.setRoles(Set.of(role));

                Post myPost = Post.builder().content("https://x.com/Morox991/status/2000313190094381390")
                        .privacy(Privacy.PUBLIC).build();
                owner.addPost(myPost);

                userRepo.save(owner);

                System.out.println("Usuario owner creado correctamente.");
            } else {
                System.out.println("Usuario de owner ya existe.");
            }
        };
    }
}
