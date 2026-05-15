package com.skybooker.NotificationService.service;

import com.skybooker.NotificationService.dto.*;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface NotificationService {
    void sendBookingConfirmation(BookingEmailRequest request);

    void sendBulkEmail(BulkEmailRequest request);

    void sendPaymentNotification(PaymentEmailRequest request);

    void sendCancellationNotification(CancellationEmailRequest request);

    public List<NotificationResponse> getAllNotifications();
}
