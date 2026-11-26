package com.cache_proxy.exceptions;

import com.cache_proxy.dtos.responses.ErrorResponse;
import com.cache_proxy.exceptions.custom.UpstreamTimeoutException;
import com.cache_proxy.exceptions.custom.UpstreamUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UpstreamTimeoutException.class)
    ResponseEntity<ErrorResponse> timeout(UpstreamTimeoutException e) {
        return error(HttpStatus.GATEWAY_TIMEOUT, "The upstream did not respond in time");
    }

    @ExceptionHandler(UpstreamUnavailableException.class)
    ResponseEntity<ErrorResponse> unavailable(UpstreamUnavailableException e) {
        return error(HttpStatus.BAD_GATEWAY, "The upstream could not be reached");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .header("X-Cache", "MISS")   // PX-5: the request went to the upstream path
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}