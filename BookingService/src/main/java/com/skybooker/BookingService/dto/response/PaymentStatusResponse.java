package com.skybooker.BookingService.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PaymentStatusResponse {
    private UUID paymentId;
    private PaymentStatus status;
}