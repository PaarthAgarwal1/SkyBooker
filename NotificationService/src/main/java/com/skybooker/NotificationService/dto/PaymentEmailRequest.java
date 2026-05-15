package com.skybooker.NotificationService.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentEmailRequest {

    private UUID userId;
    private String email;
    private String status; // SUCCESS / FAILED
    private double amount;
}
