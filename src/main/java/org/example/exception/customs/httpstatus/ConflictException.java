package org.example.exception.customs.httpstatus;

import org.springframework.http.HttpStatus;

public class ConflictException extends HttpStatusException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}