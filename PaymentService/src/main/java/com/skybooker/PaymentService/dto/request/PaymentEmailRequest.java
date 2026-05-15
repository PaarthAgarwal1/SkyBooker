package com.skybooker.PaymentService.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentEmailRequest {

    private UUID userId;
    private String email;
    private String status; // SUCCESS / FAILED
    private double amount;
}