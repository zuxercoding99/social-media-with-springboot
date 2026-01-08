package org.example.dto;

import org.example.entity.User;

public record AuthResponse(String accessToken, String refreshToken, User user) {
}