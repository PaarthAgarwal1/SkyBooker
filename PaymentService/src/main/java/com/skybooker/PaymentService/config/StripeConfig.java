package com.skybooker.PaymentService.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class
StripeConfig {

    @Value("${stripe.secret-key:}")
    private String key;

    @PostConstruct
    public void init() {
        com.stripe.Stripe.apiKey = key;
    }
}
