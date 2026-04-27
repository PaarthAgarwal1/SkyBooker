package com.skybooker.PaymentService.service.impl;

import com.skybooker.PaymentService.dto.request.InitiatePaymentRequest;
import com.skybooker.PaymentService.dto.request.ProcessPaymentRequest;
import com.skybooker.PaymentService.dto.request.RefundRequest;
import com.skybooker.PaymentService.dto.response.PaymentResponse;
import com.skybooker.PaymentService.dto.response.PaymentStatusResponse;
import com.skybooker.PaymentService.dto.response.ReceiptResponse;
import com.skybooker.PaymentService.entity.Payment;
import com.skybooker.PaymentService.entity.PaymentStatus;
import com.skybooker.PaymentService.exception.PaymentException;
import com.skybooker.PaymentService.repository.PaymentRepository;
import com.skybooker.PaymentService.service.PaymentService;
import com.skybooker.PaymentService.stripe.PaymentGateway;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
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

        p.setTransactionId(paymentGateway.generateTransactionId());

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
                params.put("amount", (long)(req.getRefundAmount() * 100));
            }

            Refund refund = Refund.create(params);

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundAmount(req.getRefundAmount());
            payment.setRefundedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            repository.save(payment);

            return map(payment);

        } catch (Exception e) {
            throw new PaymentException("Refund failed: " + e.getMessage());
        }
    }

    private PaymentResponse map(Payment p) {
        return builder()
                .paymentId(p.getPaymentId())
                .bookingId(p.getBookingId())
                .amount(p.getAmount())
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
}
