package org.example.controller;

import java.util.Map;

import org.example.dto.AuthResponse;
import org.example.dto.LoginDto;
import org.example.dto.RegisterDto;
import org.example.dto.TokenResponse;
import org.example.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-expiration}")
    private long refreshCookieMaxAgeSec;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto) {
        return ResponseEntity.ok(authService.register(registerDto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
        AuthResponse auth = authService.login(loginDto);
        addRefreshCookie(response, auth.refreshToken());
        return ResponseEntity.ok(new TokenResponse(auth.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        // Nuevo access + refresh tokens
        var authResponse = authService.refresh(refreshToken);
        addRefreshCookie(response, authResponse.refreshToken());

        return ResponseEntity.ok(new TokenResponse(authResponse.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            try {
                authService.logout(refreshToken);
            } catch (RuntimeException ignored) {
                /* token inválido ya */ }
        }

        // borrar cookie
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<?> oauthGoogle(
            @RequestBody Map<String, String> body,
            HttpServletResponse response) {
        String idToken = body.get("id_token");
        AuthResponse auth = authService.loginWithGoogle(idToken);
        addRefreshCookie(response, auth.refreshToken());
        return ResponseEntity.ok(new TokenResponse(auth.accessToken()));
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/v1/auth/")
                .maxAge(refreshCookieMaxAgeSec / 1000)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}