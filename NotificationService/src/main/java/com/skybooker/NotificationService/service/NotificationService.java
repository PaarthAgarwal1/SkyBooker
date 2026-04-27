package com.skybooker.NotificationService.service;

import com.skybooker.NotificationService.dto.BookingEmailRequest;
import com.skybooker.NotificationService.dto.BulkEmailRequest;
import com.skybooker.NotificationService.dto.CancellationEmailRequest;
import com.skybooker.NotificationService.dto.PaymentEmailRequest;
import org.springframework.scheduling.annotation.Async;

public interface NotificationService {
    void sendBookingConfirmation(BookingEmailRequest request);

    void sendBulkEmail(BulkEmailRequest request);

    void sendPaymentNotification(PaymentEmailRequest request);
    void sendCancellationNotification(CancellationEmailRequest request);
}
