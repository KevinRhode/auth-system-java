package com.authsystemjava.backend.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_CREDENTIALS("Invalid credentials", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_VERIFIED("Please verify your email before logging in", HttpStatus.FORBIDDEN),
    EMAIL_ALREADY_EXISTS("An account with this email already exists", HttpStatus.CONFLICT),
    INVALID_VERIFICATION_TOKEN("Invalid verification link", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("This link or session has expired", HttpStatus.GONE),
    INVALID_REFRESH_TOKEN("Session expired, please log in again", HttpStatus.UNAUTHORIZED),
    RATE_LIMITED("Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() { return message; }
    public HttpStatus getStatus() { return status; }
}