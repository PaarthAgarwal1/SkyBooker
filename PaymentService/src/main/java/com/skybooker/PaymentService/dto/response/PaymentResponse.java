package com.skybooker.PaymentService.dto.response;

import com.skybooker.PaymentService.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class PaymentResponse {
    private UUID paymentId;
    private UUID bookingId;
    private String username;
    private Double amount;
    private PaymentStatus status;
    private String transactionId;
    private String clientSecret;
}
