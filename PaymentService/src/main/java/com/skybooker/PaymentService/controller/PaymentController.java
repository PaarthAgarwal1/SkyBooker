package com.skybooker.PaymentService.controller;

import com.skybooker.PaymentService.dto.request.InitiatePaymentRequest;
import com.skybooker.PaymentService.dto.request.ProcessPaymentRequest;
import com.skybooker.PaymentService.dto.request.RefundRequest;
import com.skybooker.PaymentService.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(@RequestBody InitiatePaymentRequest r){
        return ResponseEntity.ok(service.initiatePayment(r));
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody ProcessPaymentRequest r){
        return ResponseEntity.ok(service.processPayment(r));
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refund(@RequestBody RefundRequest r){
        return ResponseEntity.ok(service.refundPayment(r));
    }

    @GetMapping
    public ResponseEntity<?> getAllPayments() {
        return ResponseEntity.ok(service.getAllPayments());
    }

    @GetMapping("/booking/{id}")
    public ResponseEntity<?> byBooking(@PathVariable UUID id){
        return ResponseEntity.ok(service.getPaymentByBooking(id));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> byUser(@PathVariable UUID id){
        return ResponseEntity.ok(service.getPaymentsByUser(id));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<?> status(@PathVariable UUID id){
        return ResponseEntity.ok(service.getPaymentStatus(id));
    }

    @GetMapping("/receipt/{id}")
    public ResponseEntity<?> receipt(@PathVariable UUID id){
        return ResponseEntity.ok(service.generateReceipt(id));
    }

    @GetMapping("/revenue/{id}")
    public ResponseEntity<?> revenue(@PathVariable UUID id){
        return ResponseEntity.ok(service.getRevenue(id));
    }

    @GetMapping("/analytics/{airlineId}")
    public ResponseEntity<?> analytics(
            @PathVariable UUID airlineId,
            @RequestParam(defaultValue = "monthly") String range
    ){
        return ResponseEntity.ok(service.getAnalytics(airlineId, range));
    }
}
