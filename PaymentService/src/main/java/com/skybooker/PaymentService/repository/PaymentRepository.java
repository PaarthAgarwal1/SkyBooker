package com.skybooker.PaymentService.repository;

import com.skybooker.PaymentService.entity.Payment;
import com.skybooker.PaymentService.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByBookingId(UUID bookingId);
    List<Payment> findByUserId(UUID userId);
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.userId= :userId AND p.status= 'PAID' ")
    Double sumAmountByUserId(UUID userId);
    List<Payment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);

    Optional<Payment> findByGatewayResponse(String gatewayResponse);


    Payment findTopByBookingIdOrderByCreatedAtDesc(UUID bookingId);
}
