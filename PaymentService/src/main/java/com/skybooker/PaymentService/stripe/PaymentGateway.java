package com.skybooker.PaymentService.stripe;

import com.skybooker.PaymentService.entity.PaymentStatus;
import com.stripe.model.PaymentIntent;

public interface PaymentGateway {

    PaymentIntent createOrder(Double amount);

    PaymentStatus processPayment(String id);

    String generateTransactionId();
}
