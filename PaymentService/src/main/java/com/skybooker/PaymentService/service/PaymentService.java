package com.skybooker.PaymentService.service;

import com.skybooker.PaymentService.dto.request.InitiatePaymentRequest;
import com.skybooker.PaymentService.dto.request.ProcessPaymentRequest;
import com.skybooker.PaymentService.dto.request.RefundRequest;
import com.skybooker.PaymentService.dto.response.PaymentResponse;
import com.skybooker.PaymentService.dto.response.PaymentStatusResponse;
import com.skybooker.PaymentService.dto.response.ReceiptResponse;
import com.skybooker.PaymentService.dto.response.RevenueAnalyticsResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(InitiatePaymentRequest request);

    PaymentResponse processPayment(ProcessPaymentRequest request);

    PaymentResponse refundPayment(RefundRequest request);

    PaymentResponse getPaymentByBooking(UUID bookingId);

    List<PaymentResponse> getPaymentsByUser(UUID userId);

    PaymentStatusResponse getPaymentStatus(UUID paymentId);

    ReceiptResponse generateReceipt(UUID paymentId);

    Double getRevenue(UUID userId);

    List<PaymentResponse> getAllPayments();

    RevenueAnalyticsResponse getAnalytics(UUID airlineId, String range);

    void handleSuccessfulPayment(String paymentIntentId);
}
