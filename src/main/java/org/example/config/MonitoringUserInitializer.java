package org.example.config;

import java.time.LocalDate;
import java.util.Set;

import org.example.entity.Role;
import org.example.entity.User;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class MonitoringUserInitializer {

    @Bean
    public CommandLineRunner initMonitoringUser(UserRepository userRepo,
            PasswordEncoder encoder,
            RoleRepository roleRepo) {
        return args -> {
            String username = "prometheuser";
            String rawPassword = "prometheus1234";

            if (!userRepo.existsByUsername(username)) {
                // Obtener o crear el rol
                Role role = roleRepo.findByName("ROLE_MONITORING")
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setName("ROLE_MONITORING");
                            return roleRepo.save(newRole);
                        });

                // Crear usuario
                User monitor = new User();
                monitor.setUsername(username);
                monitor.setPassword(encoder.encode(rawPassword));
                monitor.setAvatarKey("default.png");
                monitor.setEnabled(true);
                monitor.setBirthDate(LocalDate.of(1970, 1, 1));
                monitor.setDisplayName("Monitoring User");
                monitor.setEmail("monitor@system.local");
                monitor.setRoles(Set.of(role));

                userRepo.save(monitor);

                System.out.println("Usuario de monitoring creado correctamente.");
            } else {
                System.out.println("Usuario de monitoring ya existe.");
            }
        };
    }
}
