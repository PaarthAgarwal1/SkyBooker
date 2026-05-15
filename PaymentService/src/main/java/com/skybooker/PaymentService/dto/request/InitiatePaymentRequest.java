package com.skybooker.PaymentService.dto.request;

import com.skybooker.PaymentService.entity.PaymentMode;
import lombok.Data;

import java.util.UUID;

@Data
public class InitiatePaymentRequest {
    private UUID bookingId;
    private UUID userId;
    private String username;
    private String contactEmail;
    private Double amount;
    private PaymentMode paymentMode;
    private UUID flightId;
    private String route;
    private String cabinClass;
    private UUID airlineId;
}
