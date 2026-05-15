package com.skybooker.NotificationService.controller;

import com.skybooker.NotificationService.dto.*;
import com.skybooker.NotificationService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @PostMapping("/booking-confirmation")
    public ResponseEntity<String> sendBooking(@RequestBody BookingEmailRequest request) {
        service.sendBookingConfirmation(request);
        return ResponseEntity.ok("Booking email sent");
    }

    @PostMapping("/payment")
    public ResponseEntity<String> sendPayment(@RequestBody PaymentEmailRequest request) {
        service.sendPaymentNotification(request);
        return ResponseEntity.ok("Payment email sent");
    }

    @PostMapping("/cancellation")
    public ResponseEntity<String> sendCancel(@RequestBody CancellationEmailRequest request) {
        service.sendCancellationNotification(request);
        return ResponseEntity.ok("Cancellation email sent");
    }

    @PostMapping("/bulk")
    public String bulk(@RequestBody BulkEmailRequest req) {
        service.sendBulkEmail(req);
        return "Bulk Email Triggered";
    }

    @GetMapping()
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(service.getAllNotifications());
    }

}
