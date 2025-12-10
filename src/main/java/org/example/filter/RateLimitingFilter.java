package org.example.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("security");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Cache<String, Bucket> cache;

    // === Configuración desde application.properties ===
    @Value("${rate.limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${rate.limit.auth.limit:10}")
    private int authLimit;

    @Value("${rate.limit.api.limit:100}")
    private int apiLimit;

    @Value("${rate.limit.public.limit:100}")
    private int publicLimit;

    @Value("${rate.limit.admin.limit:100}")
    private int adminLimit;

    @Value("${rate.limit.default.limit:50}")
    private int defaultLimit;

    @PostConstruct
    public void init() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(windowSeconds * 2L, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        // Clave: IP + tipo de ruta
        String key = getKeyForRequest(ip, uri);

        // Obtiene o crea el bucket con su configuración
        Bucket bucket = cache.get(key, k -> createBucket(uri));
        int limit = getLimitForUri(uri);

        if (bucket.tryConsume(1)) {
            response.addHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            chain.doFilter(request, response);
        } else {
            long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(
                    bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill());
            if (waitSeconds <= 0)
                waitSeconds = 1;

            log.warn("Rate limit exceeded: ip={}, uri={}, retryAfter={}s", ip, uri, waitSeconds);

            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
            problem.setTitle("Too Many Requests");
            problem.setDetail("You have exceeded the allowed request rate. Try again later.");

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("Retry-After", String.valueOf(waitSeconds));
            response.getWriter().write(objectMapper.writeValueAsString(problem));
        }
    }

    // === Helpers ===

    private String getKeyForRequest(String ip, String uri) {
        if (uri.startsWith("/api/auth/"))
            return "auth:" + ip;
        if (uri.startsWith("/api/"))
            return "api:" + ip;
        if (uri.startsWith("/public/"))
            return "public:" + ip;
        if (uri.startsWith("/admin/"))
            return "admin:" + ip;
        return "default:" + ip;
    }

    private Bucket createBucket(String uri) {
        int limit = getLimitForUri(uri);
        Refill refill = Refill.greedy(limit, Duration.ofSeconds(windowSeconds));
        Bandwidth bandwidth = Bandwidth.classic(limit, refill);
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private int getLimitForUri(String uri) {
        if (uri.startsWith("/api/auth/"))
            return authLimit;
        if (uri.startsWith("/api/"))
            return apiLimit;
        if (uri.startsWith("/public/"))
            return publicLimit;
        if (uri.startsWith("/admin/"))
            return adminLimit;
        return defaultLimit;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/ws/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars");

    }
}
