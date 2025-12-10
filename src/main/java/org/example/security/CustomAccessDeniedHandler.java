package org.example.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger("security");

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
            AccessDeniedException ex) throws IOException {
        log.warn("403 Forbidden [{} {}] user={} ip={}",
                req.getMethod(), req.getRequestURI(),
                SecurityContextHolder.getContext().getAuthentication().getName(),
                req.getRemoteAddr());

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Access Denied");
        pd.setDetail("You do not have permission to access this resource.");

        res.setStatus(403);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(mapper.writeValueAsString(pd));
    }
}
