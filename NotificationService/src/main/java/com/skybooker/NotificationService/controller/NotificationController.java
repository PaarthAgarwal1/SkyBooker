package com.skybooker.NotificationService.controller;

import com.skybooker.NotificationService.dto.BookingEmailRequest;
import com.skybooker.NotificationService.dto.BulkEmailRequest;
import com.skybooker.NotificationService.dto.CancellationEmailRequest;
import com.skybooker.NotificationService.dto.PaymentEmailRequest;
import com.skybooker.NotificationService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
