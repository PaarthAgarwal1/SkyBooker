package com.skybooker.PaymentService.controller;

import com.skybooker.PaymentService.dto.request.PaymentEmailRequest;
import com.skybooker.PaymentService.entity.Payment;
import com.skybooker.PaymentService.entity.PaymentStatus;
import com.skybooker.PaymentService.exception.PaymentException;
import com.skybooker.PaymentService.feign.NotificationClient;
import com.skybooker.PaymentService.repository.PaymentRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
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

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final PaymentRepository repository;
    private final NotificationClient notificationClient;

    public StripeWebhookController(PaymentRepository repository,
                                   NotificationClient notificationClient) {
        this.repository = repository;
        this.notificationClient = notificationClient;
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
                    intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
                } else {
                    String json = event.getDataObjectDeserializer().getRawJson();
                    intent = PaymentIntent.GSON.fromJson(json, PaymentIntent.class);
                }

                log.info("Intent ID: {}", intent.getId());

                Payment payment = repository
                        .findByGatewayResponse(intent.getId())
                        .orElseThrow(() -> new PaymentException("Payment not found"));

                // ✅ Local variables — thread-safe
                String contactEmail = payment.getContactEmail();
                UUID userId = payment.getUserId();
                double amount = payment.getAmount();

                // ✅ Guard before doing anything else
                if (contactEmail == null || contactEmail.isBlank()) {
                    log.error("contact_email is null for payment: {}", payment.getPaymentId());
                    return ResponseEntity.ok("Webhook received but email missing");
                }

                payment.setStatus(PaymentStatus.PAID);
                payment.setTransactionId(intent.getId());
                payment.setPaidAt(LocalDateTime.now());
                repository.save(payment);

                log.info("Sending notification to: {}", contactEmail);

                // ✅ Feign call wrapped so it never breaks the 200 back to Stripe
                try {
                    notificationClient.sendPayment(
                            PaymentEmailRequest.builder()
                                    .userId(userId)
                                    .email(contactEmail)
                                    .status("SUCCESS")
                                    .amount(amount)
                                    .build()
                    );
                } catch (Exception feignEx) {
                    log.error("Notification call failed (non-fatal): {}", feignEx.getMessage());
                }
            }

            // ✅ Always return 200 to Stripe — never let internal errors cause retries
            return ResponseEntity.ok("Success");

        } catch (Exception e) {
            log.error("Webhook processing error", e);
            // ✅ Do NOT call notificationClient here — you have no safe data to send
            return ResponseEntity.status(400).body("Webhook Error");
        }
    }
}