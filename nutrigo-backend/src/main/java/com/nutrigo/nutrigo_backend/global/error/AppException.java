package com.nutrigo.nutrigo_backend.global.error;

import org.springframework.http.HttpStatus;

public abstract class AppException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    protected AppException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}