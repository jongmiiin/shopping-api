package com.skala.shopapi.exception;

import lombok.Getter;

@Getter
public class ResponseException extends RuntimeException {

    private final Error error;

    public ResponseException(Error error) {
        super(error.getMessage());
        this.error = error;
    }

    public ResponseException(Error error, String message) {
        super(message);
        this.error = error;
    }
}
