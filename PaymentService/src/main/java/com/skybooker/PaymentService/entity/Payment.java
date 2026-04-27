package com.skybooker.PaymentService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue
    private UUID paymentId;

    private UUID bookingId;
    private UUID userId;
    private String contactEmail;

    private Double amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Column(unique = true)
    private String transactionId;

    @Column(unique = true)
    private String gatewayResponse;

    private String stripePaymentIntentId;
    private String clientSecret;

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    private Double refundAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
