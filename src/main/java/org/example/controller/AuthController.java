package org.example.controller;

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
@RequestMapping("/api/auth")
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

        // set cookie HttpOnly (el frontend no puede leerla)
        ResponseCookie cookie = ResponseCookie.from("refresh_token", auth.refreshToken())
                .httpOnly(true)
                .secure(true) // true en producción con HTTPS
                .path("/api/auth/") // se enviará a /api/auth/refresh y logout
                .maxAge(refreshCookieMaxAgeSec / 1000)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // devolvemos el access token en el body
        return ResponseEntity.ok(new TokenResponse(auth.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {

        System.out.println("Refresh Token: " + refreshToken);
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        // Nuevo access + refresh tokens
        var authResponse = authService.refresh(refreshToken);

        // Actualizar cookie con el nuevo refresh token
        ResponseCookie cookie = ResponseCookie.from("refresh_token", authResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/")
                .maxAge(refreshCookieMaxAgeSec / 1000)
                .sameSite("None") // mejor compatibilidad que Strict si usás dominios distintos
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

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
                .path("/api/auth/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.noContent().build();
    }
}