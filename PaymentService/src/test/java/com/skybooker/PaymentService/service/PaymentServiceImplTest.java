package com.skybooker.PaymentService.service;

import com.skybooker.PaymentService.entity.Payment;
import com.skybooker.PaymentService.entity.PaymentStatus;
import com.skybooker.PaymentService.feign.BookingClient;
import com.skybooker.PaymentService.feign.NotificationClient;
import com.skybooker.PaymentService.repository.PaymentRepository;
import com.skybooker.PaymentService.service.impl.PaymentServiceImpl;
import com.skybooker.PaymentService.stripe.PaymentGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentGateway paymentGateway;
    @Mock private BookingClient bookingClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void getPaymentByBookingReturnsPayment() {
        UUID bookingId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .amount(5000.0)
                .status(PaymentStatus.PAID)
                .build();

        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));

        assertEquals(PaymentStatus.PAID, paymentService.getPaymentByBooking(bookingId).getStatus());
    }
}
