package com.skybooker.PaymentService.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<?> handle(PaymentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
