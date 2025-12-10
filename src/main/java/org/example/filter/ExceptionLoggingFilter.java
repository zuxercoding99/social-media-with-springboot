package org.example.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ExceptionLoggingFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger("requests");
    private static final Logger logError = LoggerFactory.getLogger("unexpected_errors");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(req, res);
        } catch (Exception ex) {
            logError.error("UNEXPECTED 500 [{} {}] user={} ip={} error={}",
                    req.getMethod(), req.getRequestURI(),
                    currentUser(), req.getRemoteAddr(), ex.getMessage(), ex);

            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            pd.setTitle("Internal Server Error");
            pd.setDetail("Unexpected error. RequestId=" + requestId);

            res.setStatus(500);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(mapper.writeValueAsString(pd));
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("[{} {}] user={} ip={} status={} duration={}ms requestId={}",
                    req.getMethod(), req.getRequestURI(),
                    currentUser(), req.getRemoteAddr(), res.getStatus(), duration, requestId);
            MDC.clear();
        }
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }
}