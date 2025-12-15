package org.example.service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.dto.AuthResponse;
import org.example.dto.LoginDto;
import org.example.dto.RegisterDto;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.ConflictException;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.exception.customs.httpstatus.UnauthorizedException;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Transactional
    public String register(RegisterDto registerDto) {

        if (userRepository.existsByUsername(registerDto.username())) {
            throw new ConflictException("El username ya está en uso");
        }

        if (userRepository.existsByEmail(registerDto.email())) {
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
        User user = new User();
        user.setEmail(registerDto.email());
        user.setUsername(registerDto.username());
        user.setPassword(passwordEncoder.encode(registerDto.password()));
        user.setDisplayName(registerDto.username());
        user.setBirthDate(registerDto.birthDate());
        user.getRoles().add(roleUser);

        userRepository.save(user);

        return "Usuario registrado con éxito!";
    }

    @Transactional
    public AuthResponse login(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new UsernameNotFoundException("No existe usuario con ese email"));

        // autenticación (lanza excepción si falla)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginDto.password()));

        // generar access token (JWT)
        String accessToken = jwtUtil.generateToken(user.getUsername(), authentication.getAuthorities());

        // crear/actualizar refresh token en BD
        var refreshTokenEntity = refreshTokenService.createOrReplace(user);

        // devolvemos ambos (controller manejará la cookie)
        return new AuthResponse(accessToken, refreshTokenEntity.getToken());
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
        return new AuthResponse(newAccessToken, newRefreshTokenEntity.getToken());
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

        return userRepository.findByUsername(username)
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

        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<String> getCurrentUsernameOptional() {
        return getCurrentUserOptional()
                .map(User::getUsername);
    }

}
