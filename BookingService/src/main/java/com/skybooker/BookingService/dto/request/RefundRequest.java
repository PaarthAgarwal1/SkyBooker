package com.skybooker.BookingService.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RefundRequest {
    private UUID paymentId;
    private Double refundAmount;
}