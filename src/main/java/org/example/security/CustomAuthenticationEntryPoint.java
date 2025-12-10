package org.example.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger("security");

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
            AuthenticationException ex) throws IOException {
        log.warn("401 Unauthorized [{} {}] ip={}", req.getMethod(), req.getRequestURI(), req.getRemoteAddr());

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Unauthorized");
        pd.setDetail("Authentication is required to access this resource.");

        res.setStatus(401);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(mapper.writeValueAsString(pd));
    }
}