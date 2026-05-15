package com.skybooker.PaymentService.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class ConfirmBookingRequest {
    private UUID paymentId;
}
