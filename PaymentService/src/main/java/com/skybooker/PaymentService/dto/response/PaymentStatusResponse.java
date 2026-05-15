package com.skybooker.PaymentService.dto.response;

import com.skybooker.PaymentService.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PaymentStatusResponse {
    private UUID paymentId;
    private PaymentStatus status;
}