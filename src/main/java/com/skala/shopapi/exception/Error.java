package com.skala.shopapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum Error {

    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "Data not found"),
    DATA_DUPLICATED(HttpStatus.CONFLICT, "Data duplicated"),
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "Insufficient funds"),
    INSUFFICIENT_QUANTITY(HttpStatus.BAD_REQUEST, "Insufficient quantity"),
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "Not authenticated"),
    PURCHASE_REQUIRED(HttpStatus.FORBIDDEN, "Purchase required before writing a review");

    private final HttpStatus httpStatus;
    private final String message;

    Error(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
