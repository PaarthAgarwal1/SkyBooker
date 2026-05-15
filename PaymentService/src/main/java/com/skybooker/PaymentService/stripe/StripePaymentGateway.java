package com.skybooker.PaymentService.stripe;

import com.skybooker.PaymentService.entity.PaymentStatus;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StripePaymentGateway implements PaymentGateway {

    @Override
    public PaymentIntent createOrder(Double amount) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", (long)(amount * 100));
            params.put("currency", "inr");
            params.put("payment_method_types", List.of("card"));

            PaymentIntent intent = PaymentIntent.create(params);

            return intent; // ✅ IMPORTANT
        } catch (Exception e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    @Override
    public PaymentStatus processPayment(String id) {
        throw new UnsupportedOperationException("Use webhook for payment confirmation");
    }

    @Override
    public String generateTransactionId() {
        return "txn_" + UUID.randomUUID().toString().substring(0, 10);
    }
}
