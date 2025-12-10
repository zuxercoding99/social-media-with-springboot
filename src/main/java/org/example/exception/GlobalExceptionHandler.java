package org.example.exception;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import org.example.exception.customs.httpstatus.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger errorLog = LoggerFactory.getLogger("errors");
    private static final Logger unexpectedErrorLog = LoggerFactory.getLogger("unexpected_errors");

    // Helper para crear ProblemDetail
    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setProperty("requestId", MDC.get("requestId"));
        return problem;
    }

    // Helpers de logging
    private String getUserFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser"))
                ? auth.getName()
                : "anonymous";
    }

    private String maskAuthorizationHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return "none";
        }
        if (authHeader.startsWith("Bearer ")) {
            return "Bearer [masked]";
        }
        return "[masked]";
    }

    private void logError(Logger logger, String level, Exception ex, ServletWebRequest request, String message,
            Object... args) {
        String requestId = MDC.get("requestId");
        String method = request.getHttpMethod().name();
        String uri = request.getRequest().getRequestURI();
        String clientIp = request.getRequest().getRemoteAddr();
        String user = getUserFromSecurityContext();
        String creds = (ex instanceof AuthenticationException || ex instanceof JwtException
                || ex instanceof AccessDeniedException)
                        ? maskAuthorizationHeader(request.getRequest().getHeader("Authorization"))
                        : "";
        String logMessage = String.format(
                "%s [type=%s, method=%s, uri=%s, user=%s, ip=%s%s, requestId=%s] %s",
                ex.getClass().getSimpleName(), ex.getClass().getSimpleName(), method, uri, user, clientIp,
                creds.isEmpty() ? "" : ", creds=" + creds,
                requestId != null ? requestId : "unknown", message);

        if ("ERROR".equalsIgnoreCase(level)) {
            logger.error(logMessage, args, ex);
        } else {
            logger.warn(logMessage, args);
        }
    }

    // --- Overrides con logs ---
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (existing, replacement) -> existing));

        String errorDetails = errors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));

        logError(errorLog, "WARN", ex, (ServletWebRequest) request, "Validation failed: {}", errorDetails);

        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation Error",
                "One or more fields have errors.");
        problem.setProperty("errors", errors);

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String detail = ex.getMostSpecificCause().getMessage();
        logError(errorLog, "WARN", ex, (ServletWebRequest) request, "Invalid JSON: {}", detail);

        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid JSON",
                "The request body is not valid JSON: " + detail);

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    // --- Handlers con logs ---
    @ExceptionHandler(HttpStatusException.class)
    public ResponseEntity<ProblemDetail> handleHttpStatusException(HttpStatusException ex, WebRequest request) {
        logError(errorLog, "WARN", ex, (ServletWebRequest) request, ex.getMessage());
        ProblemDetail problem = buildProblemDetail(ex.getStatus(), ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        logError(errorLog, "WARN", ex, (ServletWebRequest) request, ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex,
            WebRequest request) {
        logError(errorLog, "ERROR", ex, (ServletWebRequest) request, "Data integrity violation: {}",
                ex.getMostSpecificCause().getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Data Integrity Violation",
                "The operation violates database constraints.");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex,
            WebRequest request) {
        String details = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));
        logError(errorLog, "WARN", ex, (ServletWebRequest) request, "Constraint violation: {}", details);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation Error", details);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        logError(errorLog, "WARN", ex, (ServletWebRequest) request, ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Argument", ex.getMessage());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logError(errorLog, "WARN", ex, (ServletWebRequest) request, "Authentication failed: {}", ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNAUTHORIZED, "Authentication Failed",
                "Invalid credentials or session expired.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logError(errorLog, "WARN", ex, (ServletWebRequest) request, "Access denied: {}", ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.FORBIDDEN, "Access Denied",
                "You do not have sufficient permissions to access this resource.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ProblemDetail> handleJwtException(JwtException ex, WebRequest request) {
        logError(errorLog, "WARN", ex, (ServletWebRequest) request, "Invalid JWT: {}", ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid JWT Token", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllExceptions(Exception ex, WebRequest request) {
        logError(unexpectedErrorLog, "ERROR", ex, (ServletWebRequest) request, "Unexpected error: {}", ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
