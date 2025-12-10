package org.example.exception.customs.httpstatus;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HttpStatusException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
