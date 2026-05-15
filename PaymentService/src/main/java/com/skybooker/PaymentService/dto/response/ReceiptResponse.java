package com.skybooker.PaymentService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Data
public class ReceiptResponse {
    private String receiptId;
    private UUID paymentId;
    private UUID bookingId;
    private Double amount;
    private String status;
    private LocalDateTime paidAt;
}
