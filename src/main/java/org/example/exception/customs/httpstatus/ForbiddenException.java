package org.example.exception.customs.httpstatus;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends HttpStatusException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}