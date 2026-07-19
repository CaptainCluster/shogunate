package com.tvtracker.common.exception;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(message, 401);
    }
}
