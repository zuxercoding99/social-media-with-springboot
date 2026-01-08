package org.example.service;

import org.example.entity.RefreshToken;
import org.example.entity.User;
import org.example.exception.customs.InvalidTokenException;
import org.example.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repo,
            @Value("${jwt.refresh-expiration}") long refreshExpirationMs) {
        this.repo = repo;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public RefreshToken createOrReplace(User user) {
        var existing = repo.findByUser(user).orElse(null);
        if (existing != null) {
            existing.setToken(UUID.randomUUID().toString());
            existing.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
            return repo.save(existing); // UPDATE
        }

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        return repo.save(rt);
    }

    @Transactional
    public RefreshToken validateAndGet(String token) {
        System.out.println("refresh token: " + token);
        RefreshToken refreshToken = repo.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            repo.deleteByToken(token); // DELETE directo, sin flush manual del token expirado
            throw new InvalidTokenException("Expired refresh token");
        }

        return refreshToken;
    }

    @Transactional
    public void deleteByUser(User user) {
        repo.deleteByUser(user);
    }
}