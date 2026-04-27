package com.skybooker.NotificationService.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CancellationEmailRequest {

    private UUID userId;
    private String email;
    private String pnr;
    private double refundAmount;
}
