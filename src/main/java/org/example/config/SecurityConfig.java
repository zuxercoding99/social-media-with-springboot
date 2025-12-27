
package org.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.filter.ExceptionLoggingFilter;
import org.example.filter.RateLimitingFilter;
import org.example.repository.UserRepository;
import org.example.security.CustomAuthenticationEntryPoint;
import org.example.security.CustomAccessDeniedHandler;
import org.example.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${cors.allowed-origins}") // Inyecta como String comma-separated y lo convertimos a List
    private String allowedOriginsString;

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    public SecurityConfig(JwtUtil jwtUtil, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;

    }

    // 1. Password encoder
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Cargar usuarios desde base de datos
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.isEnabled(),
                    true,
                    true,
                    true,
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getName()))
                            .collect(Collectors.toSet()));
        };
    }

    // 4. AuthenticationManager para login con email/password
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 5. Filtro JWT: agrega el usuario al contexto si el token es válido
    @Bean
    public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getRequestURI();
                return path.startsWith("/api/v1/auth/")
                        || path.startsWith("/h2-console/")
                        || path.startsWith("/ws/");
            }

            @Override
            protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                    throws ServletException, IOException {

                String authHeader = req.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.extractUsername(token);

                        if (SecurityContextHolder.getContext().getAuthentication() == null) {
                            UserDetails userDetails = userDetailsService().loadUserByUsername(username);

                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                }

                chain.doFilter(req, res);
            }
        };
    }

    // 6. Configuración de CORS (opcional pero recomendado)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Convierte el String comma-separated a List<String>
        List<String> allowedOrigins = Arrays.asList(allowedOriginsString.split(","));

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 7. Cadena de filtros principal
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            CustomAuthenticationEntryPoint entryPoint,
            CustomAccessDeniedHandler deniedHandler, RateLimitingFilter rateLimitingFilter,
            ExceptionLoggingFilter exceptionLoggingFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(Customizer.withDefaults()) // ➜ Habilita Basic Auth
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("MONITORING")
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/h2-console/**",
                                "/ws/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // Para H2
                // ORDEN PERSONALIZADO:
                // 1. Rate limiting (primero, bloquea abuso)
                // 2. Exception logging (segundo, captura errores de filtros siguientes)
                // 3. JWT filter (autenticación)
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(exceptionLoggingFilter, RateLimitingFilter.class)
                .addFilterAfter(jwtFilter(), ExceptionLoggingFilter.class);

        return http.build();
    }
}
