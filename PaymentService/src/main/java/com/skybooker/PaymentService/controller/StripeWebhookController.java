package com.skybooker.PaymentService.controller;

import com.skybooker.PaymentService.dto.request.PaymentEmailRequest;
import com.skybooker.PaymentService.entity.Payment;
import com.skybooker.PaymentService.entity.PaymentStatus;
import com.skybooker.PaymentService.exception.PaymentException;
import com.skybooker.PaymentService.feign.NotificationClient;
import com.skybooker.PaymentService.repository.PaymentRepository;
import com.skybooker.PaymentService.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/stripe/webhook")
public class StripeWebhookController {

    @Value("${stripe.webhook-secret:}")
    private String endpointSecret;

    private final PaymentRepository repository;
    private final NotificationClient notificationClient;
    private final PaymentService paymentService;

    public StripeWebhookController(PaymentRepository repository,
                                   NotificationClient notificationClient,
                                   PaymentService paymentService) {
        this.repository = repository;
        this.notificationClient = notificationClient;
        this.paymentService = paymentService;
    }

    // ✅ NO instance variables for request state — always local only
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {

            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            log.info("Webhook received: {}", event.getType());

            if ("payment_intent.succeeded".equals(event.getType())) {

                PaymentIntent intent;

                if (event.getDataObjectDeserializer().getObject().isPresent()) {

                    intent = (PaymentIntent) event.getDataObjectDeserializer()
                            .getObject()
                            .get();

                } else {

                    String json = event.getDataObjectDeserializer().getRawJson();

                    intent = PaymentIntent.GSON.fromJson(json, PaymentIntent.class);
                }

                log.info("Intent ID: {}", intent.getId());

                try {

                    paymentService.handleSuccessfulPayment(intent.getId());

                    log.info("Payment processed successfully");

                } catch (Exception ex) {

                    log.error("Payment processing failed", ex);
                }
            }

            return ResponseEntity.ok("Success");

        } catch (Exception e) {

            log.error("Webhook processing error", e);

            return ResponseEntity.status(400).body("Webhook Error");
        }
    }
}
