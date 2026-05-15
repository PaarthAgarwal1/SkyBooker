package com.skybooker.PaymentService.exception;

public class PaymentException extends RuntimeException {
    public PaymentException(String msg) {
        super(msg);
    }
}