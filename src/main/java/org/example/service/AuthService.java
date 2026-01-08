package org.example.service;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.dto.AuthResponse;
import org.example.dto.LoginDto;
import org.example.dto.RegisterDto;
import org.example.entity.AuthProvider;
import org.example.entity.Role;
import org.example.entity.ThemeMode;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.ConflictException;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.exception.customs.httpstatus.UnauthorizedException;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final JwtDecoder googleJwtDecoder;

    // ---------------- REGISTER LOCAL ----------------
    @Transactional
    public String register(RegisterDto registerDto) {

        String username = registerDto.username().trim().toLowerCase(Locale.ROOT);
        String email = registerDto.email().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("El username ya está en uso");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("El email ya está en uso");
        }

        // Buscar o crear ROLE_USER
        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });

        // Crear usuario
        User user = User.builder()
                .email(registerDto.email().toLowerCase())
                .username(registerDto.username().toLowerCase())
                .displayName(registerDto.username())
                .bio("")
                .password(passwordEncoder.encode(registerDto.password().trim()))
                .birthDate(registerDto.birthDate())
                .bannerColor("#1da1f2")
                .authProvider(AuthProvider.LOCAL)
                .localPasswordSet(true)
                .themeMode(ThemeMode.LIGHT)
                .avatarKey("default.png")
                .enabled(true)
                .roles(Set.of(roleUser))
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("El email o username ya está en uso");
        }

        return "Usuario registrado con éxito!";
    }

    // ---------------- LOGIN LOCAL ----------------
    @Transactional
    public AuthResponse login(LoginDto loginDto) {

        User user = userRepository.findByEmail(loginDto.email().trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("No existe usuario con ese email"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Usuario deshabilitado");
        }

        if (!user.isLocalPasswordSet()) {
            throw new UnauthorizedException("Este usuario debe loguearse con OAuth");
        }

        // autenticación (lanza excepción si falla)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginDto.password().trim()));

        // generar access token (JWT)
        String accessToken = jwtUtil.generateToken(user.getUsername(), authentication.getAuthorities());

        // crear/actualizar refresh token en BD
        var refreshTokenEntity = refreshTokenService.createOrReplace(user);

        // devolvemos ambos (controller manejará la cookie)
        return new AuthResponse(accessToken, refreshTokenEntity.getToken(), user);
    }

    // ---------------- LOGIN OAUTH GOOGLE ----------------
    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {

        Jwt jwt;
        try {
            jwt = googleJwtDecoder.decode(idToken);
        } catch (JwtException e) {
            throw new UnauthorizedException("Token Google inválido");
        }

        String email = jwt.getClaimAsString("email");
        Boolean verified = jwt.getClaimAsBoolean("email_verified");
        String providerId = jwt.getSubject();
        String name = jwt.getClaimAsString("name");

        if (email == null || !Boolean.TRUE.equals(verified)) {
            throw new UnauthorizedException("Email no verificado");
        }

        User user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> userRepository.findByEmail(email.toLowerCase())
                        .map(existing -> {
                            existing.setProviderId(providerId);
                            return existing;
                        })
                        .orElseGet(() -> createOAuthUser(email, providerId, name)));

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ya existe un usuario con ese email");
        }

        String accessToken = jwtUtil.generateToken(
                user.getUsername(),
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .collect(Collectors.toSet()));

        var refresh = refreshTokenService.createOrReplace(user);
        return new AuthResponse(accessToken, refresh.getToken(), user);
    }

    // ---------------- HELPERS ----------------
    private User createOAuthUser(String email, String providerId, String name) {

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        String baseUsername = email.split("@")[0];
        String username = generateUniqueUsername(baseUsername);

        User user = User.builder()
                .email(email.toLowerCase())
                .username(username)
                .displayName(name != null ? name : username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .bio("")
                .bannerColor("#1da1f2")
                .authProvider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .localPasswordSet(false)
                .birthDate(LocalDate.now().minusYears(18))
                .themeMode(ThemeMode.LIGHT)
                .avatarKey("default.png")
                .enabled(true)
                .roles(Set.of(roleUser))
                .build();

        return user;
    }

    /* Puede mejorar */
    private String generateUniqueUsername(String base) {
        String candidate = base.toLowerCase();
        int i = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = base + i++;
        }
        return candidate;
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        // Validar el refresh token actual
        var tokenEntity = refreshTokenService.validateAndGet(refreshToken);
        User user = tokenEntity.getUser();

        // actualizar directamente el token existente
        var newRefreshTokenEntity = refreshTokenService.createOrReplace(user);

        // Generar un nuevo access token
        String newAccessToken = jwtUtil.generateToken(
                user.getUsername(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toSet()));

        // Devolver ambos (el controlador pondrá el nuevo refresh token en cookie)
        return new AuthResponse(newAccessToken, newRefreshTokenEntity.getToken(), user);
    }

    @Transactional
    public void logout(String refreshToken) {
        var tokenEntity = refreshTokenService.validateAndGet(refreshToken);
        refreshTokenService.deleteByUser(tokenEntity.getUser());
    }

    // Metodos para endpoints que requieren al usuario autenticado
    @Transactional(readOnly = true)
    public User getCurrentUser() {

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No autenticado");
        }

        String username = authentication.getName();

        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // Metodos recomendados para endpoints que estar autenticado es opcional
    @Transactional(readOnly = true)
    public Optional<User> getCurrentUserOptional() {

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        // No hay contexto o no está autenticado
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        String username = authentication.getName();

        return userRepository.findByUsernameIgnoreCase(username);
    }

    @Transactional(readOnly = true)
    public Optional<String> getCurrentUsernameOptional() {
        return getCurrentUserOptional()
                .map(User::getUsername);
    }

}
