package com.skybooker.BookingService.feign;

import com.skybooker.BookingService.dto.request.RefundRequest;
import com.skybooker.BookingService.dto.response.PaymentStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {
    @PostMapping("/payments/refund")
    public ResponseEntity<?> refund(@RequestBody RefundRequest r);

    @GetMapping("/payments/status/{id}")
    PaymentStatusResponse getPaymentStatus(@PathVariable UUID id);
}
