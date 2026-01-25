package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class StartupLogger {

    @Autowired
    private Environment env;

    @PostConstruct
    public void logProfile() {
        System.out.println("🔵 Perfil activo: " + String.join(", ", env.getActiveProfiles()));
    }
}
