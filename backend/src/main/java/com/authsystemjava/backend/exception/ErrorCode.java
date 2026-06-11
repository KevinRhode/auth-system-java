package com.authsystemjava.backend.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_CREDENTIALS("Invalid credentials", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_VERIFIED("Please verify your email before logging in", HttpStatus.FORBIDDEN),
    INVALID_VERIFICATION_TOKEN("Invalid verification link", HttpStatus.BAD_REQUEST),
    INVALID_RESET_TOKEN("This password reset link is invalid or has expired", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("This link or session has expired", HttpStatus.GONE),
    INVALID_REFRESH_TOKEN("Session expired, please log in again", HttpStatus.UNAUTHORIZED),
    RATE_LIMITED("Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("Could not find user",HttpStatus.NOT_FOUND),
    COMPANY_NOT_FOUND("Company not found", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("Member of Company Not Found", HttpStatus.NOT_FOUND),
    ALREADY_IN_COMPANY("Already Assigned to a Company", HttpStatus.CONFLICT),
    NOT_A_MEMBER("Not a Member",HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSIONS("Need Permissions", HttpStatus.FORBIDDEN),
    CANNOT_MODIFY_OWNER("Is Owner", HttpStatus.BAD_REQUEST),
    INVALID_ROLE("Invalid Role", HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() { return message; }
    public HttpStatus getStatus() { return status; }
}