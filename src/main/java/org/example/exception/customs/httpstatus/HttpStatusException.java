package org.example.exception.customs.httpstatus;

import org.springframework.http.HttpStatus;

public abstract class HttpStatusException extends RuntimeException {
    private final HttpStatus status;

    protected HttpStatusException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}