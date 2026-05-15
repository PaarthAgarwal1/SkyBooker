package com.skybooker.PaymentService.service.impl;

import com.skybooker.PaymentService.dto.request.*;
import com.skybooker.PaymentService.dto.response.*;
import com.skybooker.PaymentService.entity.Payment;
import com.skybooker.PaymentService.entity.PaymentStatus;
import com.skybooker.PaymentService.exception.PaymentException;

import com.skybooker.PaymentService.feign.BookingClient;
import com.skybooker.PaymentService.feign.NotificationClient;
import com.skybooker.PaymentService.repository.PaymentRepository;
import com.skybooker.PaymentService.service.PaymentService;
import com.skybooker.PaymentService.stripe.PaymentGateway;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.skybooker.PaymentService.dto.response.PaymentResponse.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentGateway paymentGateway;
    private final BookingClient bookingClient;
    private final NotificationClient notificationClient;

    @Override
    public PaymentResponse initiatePayment(InitiatePaymentRequest req ) {
        Payment last = repository.findTopByBookingIdOrderByCreatedAtDesc(req.getBookingId());

        if (last != null) {

            if (last.getStatus() == PaymentStatus.PAID) {
                throw new PaymentException("Booking already paid");
            }

            if (last.getStatus() == PaymentStatus.PENDING) {
                return map(last);
            }
        }

        PaymentIntent intent = paymentGateway.createOrder(req.getAmount());
        Payment p = Payment.builder()
                .bookingId(req.getBookingId())
                .userId(req.getUserId())
                .username(req.getUsername())
                .flightId(req.getFlightId())
                .route(req.getRoute())
                .cabinClass(req.getCabinClass())
                .airlineId(req.getAirlineId())
                .amount(req.getAmount())
                .currency("INR")
                .contactEmail(req.getContactEmail())
                .clientSecret(intent.getClientSecret())
                .status(PaymentStatus.PENDING)
                .paymentMode(req.getPaymentMode())
                .gatewayResponse(intent.getId())
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(p);
        return map(p);
    }

    @Override
    public PaymentResponse processPayment(ProcessPaymentRequest request) {

        Payment p=repository.findById(request.getPaymentId())
                .orElseThrow(()->new PaymentException("Payment not found"));

        if(p.getStatus() !=PaymentStatus.PENDING){
            throw new PaymentException("Already processed");
        }
        PaymentStatus result=paymentGateway.processPayment(p.getGatewayResponse());

        p.setTransactionId(p.getGatewayResponse());

        if (result==PaymentStatus.PAID){
            p.setStatus(PaymentStatus.PAID);
            p.setPaidAt(LocalDateTime.now());
        }else {
            p.setStatus(PaymentStatus.FAILED);
        }

        repository.save(p);

        return map(p);
    }

    @Override
    public PaymentResponse refundPayment(RefundRequest req) {

        Payment payment = repository.findById(req.getPaymentId())
                .orElseThrow(() -> new PaymentException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new PaymentException("Only PAID payments can be refunded");
        }

        try {

            Map<String, Object> params = new HashMap<>();
            params.put("payment_intent", payment.getTransactionId());

            if (req.getRefundAmount() != null) {
                params.put("amount", (long) (req.getRefundAmount() * 100));
            }

            Refund refund = Refund.create(params);

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundAmount(req.getRefundAmount());
            payment.setRefundedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            repository.save(payment);

            try {

                notificationClient.sendPayment(
                        PaymentEmailRequest.builder()
                                .userId(payment.getUserId())
                                .email(payment.getContactEmail())
                                .status("REFUNDED")
                                .amount(
                                        req.getRefundAmount() != null
                                                ? req.getRefundAmount()
                                                : payment.getAmount()
                                )
                                .build()
                );

            } catch (Exception ex) {

                System.err.println(
                        "Refund notification failed: " + ex.getMessage()
                );
            }

            return map(payment);

        } catch (Exception e) {

            throw new PaymentException(
                    "Refund failed: " + e.getMessage()
            );
        }
    }

    private PaymentResponse map(Payment p) {

        return builder()
                .paymentId(p.getPaymentId())
                .bookingId(p.getBookingId())
                .amount(p.getAmount())
                .username(p.getUsername())
                .status(p.getStatus())
                .transactionId(p.getTransactionId())
                .clientSecret(p.getClientSecret())
                .build();
    }

    @Override public PaymentResponse getPaymentByBooking(UUID id){return map(repository.findByBookingId(id).orElseThrow());}
    @Override public List<PaymentResponse> getPaymentsByUser(UUID id){return repository.findByUserId(id).stream().map(this::map).toList();}
    @Override public PaymentStatusResponse getPaymentStatus(UUID id){return new PaymentStatusResponse(id, repository.findById(id).orElseThrow().getStatus());}
    @Override public ReceiptResponse generateReceipt(UUID id){Payment p=repository.findById(id).orElseThrow();return new ReceiptResponse("RCPT-"+id,id,p.getBookingId(),p.getAmount(),p.getStatus().name(),p.getPaidAt());}
    @Override public Double getRevenue(UUID id){return repository.sumAmountByUserId(id);}

    @Override
    public List<PaymentResponse> getAllPayments() {
        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public RevenueAnalyticsResponse getAnalytics(UUID airlineId, String range) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime currentStart;
        LocalDateTime previousStart;

        // ===============================
        // RANGE HANDLING
        // ===============================

        if ("weekly".equalsIgnoreCase(range)) {

            currentStart = now.minusDays(7);
            previousStart = now.minusDays(14);

        } else {

            currentStart = now.minusDays(30);
            previousStart = now.minusDays(60);
        }

        // ===============================
        // CURRENT PERIOD PAYMENTS
        // ===============================

        List<Payment> currentPayments =
                repository.findPaidPaymentsByAirlineAndDateRange(
                        airlineId,
                        currentStart,
                        now
                );

        // ===============================
        // PREVIOUS PERIOD PAYMENTS
        // ===============================

        List<Payment> previousPayments =
                repository.findPaidPaymentsByAirlineAndDateRange(
                        airlineId,
                        previousStart,
                        currentStart
                );

        // ===============================
        // TOTAL REVENUE
        // ===============================

        double totalRevenue = currentPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        double previousRevenue = previousPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        // ===============================
        // GROWTH %
        // ===============================

        double growthPercentage = 0;

        if (previousRevenue > 0) {

            growthPercentage =
                    ((totalRevenue - previousRevenue)
                            / previousRevenue) * 100;
        }

        // ===============================
        // ROUTE REVENUE
        // ===============================

        Map<String, Double> routeMap = new HashMap<>();

        for (Payment p : currentPayments) {

            String route =
                    p.getRoute() != null
                            ? p.getRoute()
                            : "UNKNOWN";

            routeMap.merge(
                    route,
                    p.getAmount(),
                    Double::sum
            );
        }

        List<RouteRevenue> routeRevenueList =
                routeMap.entrySet()
                        .stream()
                        .map(e ->
                                new RouteRevenue(
                                        e.getKey(),
                                        e.getValue()
                                )
                        )
                        .toList();

        // ===============================
        // CABIN DISTRIBUTION
        // ===============================

        Map<String, Double> cabinCount = new HashMap<>();

        for (Payment p : currentPayments) {

            String cabin =
                    p.getCabinClass() != null
                            ? p.getCabinClass()
                            : "ECONOMY";

            cabinCount.merge(
                    cabin,
                    1.0,
                    Double::sum
            );
        }

        double totalBookings = currentPayments.size();

        if (totalBookings == 0) {
            totalBookings = 1;
        }

        CabinDistribution cabinDistribution =
                CabinDistribution.builder()
                        .economy(
                                (cabinCount.getOrDefault("ECONOMY", 0.0)
                                        / totalBookings) * 100
                        )
                        .business(
                                (cabinCount.getOrDefault("BUSINESS", 0.0)
                                        / totalBookings) * 100
                        )
                        .firstClass(
                                (cabinCount.getOrDefault("FIRST_CLASS", 0.0)
                                        / totalBookings) * 100
                        )
                        .build();

        // ===============================
        // REVENUE TRENDS
        // ===============================

        Map<String, Double> trendMap = new TreeMap<>();

        for (Payment p : currentPayments) {

            if (p.getPaidAt() == null) {
                continue;
            }

            String date =
                    p.getPaidAt()
                            .toLocalDate()
                            .toString();

            trendMap.merge(
                    date,
                    p.getAmount(),
                    Double::sum
            );
        }

        List<RevenueTrend> revenueTrends =
                trendMap.entrySet()
                        .stream()
                        .map(e ->
                                new RevenueTrend(
                                        e.getKey(),
                                        e.getValue()
                                )
                        )
                        .toList();

        // ===============================
        // FINAL RESPONSE
        // ===============================

        RevenueAnalyticsResponse rar= RevenueAnalyticsResponse.builder()
                .totalRevenue(totalRevenue)
                .growthPercentage(
                        Math.round(growthPercentage * 100.0) / 100.0
                )
                .routeRevenue(routeRevenueList)
                .cabinDistribution(cabinDistribution)
                .revenueTrends(revenueTrends)
                .build();
        return rar;

    }

    @Override
    @Transactional
    public void handleSuccessfulPayment(String paymentIntentId) {

        Payment payment = repository.findByGatewayResponse(paymentIntentId)
                .orElseThrow(() -> new PaymentException(
                        "Payment not found for Intent: " + paymentIntentId));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(paymentIntentId);
        payment.setPaidAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        repository.save(payment);

        // ONLY notification here

        try {
            notificationClient.sendPayment(
                    PaymentEmailRequest.builder()
                            .userId(payment.getUserId())
                            .email(payment.getContactEmail())
                            .status("SUCCESS")
                            .amount(payment.getAmount())
                            .build()
            );
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
    }
}
