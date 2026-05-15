package com.skybooker.PaymentService.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class RefundRequest {
    private UUID paymentId;
    private Double refundAmount;
}