package com.skala.shopapi.exception;

import com.skala.shopapi.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<Response<Void>> handleResponseException(ResponseException e) {
        Error error = e.getError();
        return ResponseEntity.status(error.getHttpStatus())
                .body(Response.of(error.getHttpStatus().value(), e.getMessage(), null));
    }

    @ExceptionHandler(ParameterException.class)
    public ResponseEntity<Response<Void>> handleParameterException(ParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.of(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
    }

    /**
     * 매핑된 핸들러가 없는 경로(예: "/", 오타 URL, 존재하지 않는 정적 리소스)에 대해 Spring이 던지는 예외.
     * 아래 catch-all(Exception.class)에 걸려 500으로 둔갑하지 않도록 404로 명시적으로 처리한다.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Response.of(HttpStatus.NOT_FOUND.value(), "No handler found for this path", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", null));
    }
}
