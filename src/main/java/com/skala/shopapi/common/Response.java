package com.skala.shopapi.common;

import lombok.Getter;

@Getter
public class Response<T> {

    private final int code;
    private final String message;
    private final T data;

    private Response(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(200, "OK", data);
    }

    public static <T> Response<T> success(String message, T data) {
        return new Response<>(200, message, data);
    }

    public static <T> Response<T> of(int code, String message, T data) {
        return new Response<>(code, message, data);
    }
}
