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

    @Query("SELECT p FROM Payment p WHERE p.status = 'PAID' AND p.airlineId = :airlineId")
    List<Payment> findAllPaidPaymentsByAirline(UUID airlineId);
    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.airlineId = :airlineId
        AND p.status = 'PAID'
        AND p.paidAt BETWEEN :startDate AND :endDate
    """)
    List<Payment> findPaidPaymentsByAirlineAndDateRange(
            UUID airlineId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
