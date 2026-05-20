package com.skybooker.PaymentService.repository;

import com.skybooker.PaymentService.entity.Payment;
import com.skybooker.PaymentService.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void saveAndFindByBookingId() {
        UUID bookingId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .userId(UUID.randomUUID())
                .amount(5000.0)
                .status(PaymentStatus.PAID)
                .gatewayResponse("pi_test")
                .build();

        paymentRepository.save(payment);

        assertTrue(paymentRepository.findByBookingId(bookingId).isPresent());
        assertEquals(PaymentStatus.PAID, paymentRepository.findByBookingId(bookingId).get().getStatus());
    }
}
