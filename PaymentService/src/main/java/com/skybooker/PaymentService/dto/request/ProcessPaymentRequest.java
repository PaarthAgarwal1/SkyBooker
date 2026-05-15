package com.skybooker.PaymentService.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ProcessPaymentRequest {
    private UUID paymentId;
}
