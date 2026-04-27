package com.skybooker.PaymentService.dto.request;

import com.skybooker.PaymentService.entity.PaymentMode;
import lombok.Data;

import java.util.UUID;

@Data
public class InitiatePaymentRequest {
    private UUID bookingId;
    private UUID userId;
    private String contactEmail;
    private Double amount;
    private PaymentMode paymentMode;
}
