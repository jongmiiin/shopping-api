package com.skala.shopapi.exception;

public class ParameterException extends RuntimeException {

    public ParameterException(String... fieldNames) {
        super("Invalid or missing parameter(s): " + String.join(", ", fieldNames));
    }
}
