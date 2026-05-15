package com.skybooker.NotificationService.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skybooker.NotificationService.dto.*;
import com.skybooker.NotificationService.entity.Notification;
import com.skybooker.NotificationService.entity.NotificationStatus;
import com.skybooker.NotificationService.entity.NotificationType;
import com.skybooker.NotificationService.repository.NotificationRepository;
import com.skybooker.NotificationService.service.NotificationService;
import com.skybooker.NotificationService.util.PdfGenerator;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository repository;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    public NotificationServiceImpl(NotificationRepository repository,
                                   JavaMailSender mailSender,
                                   TemplateEngine templateEngine,
                                   ObjectMapper objectMapper) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ================= BOOKING =================

    @Async
    @Override
    public void sendBookingConfirmation(BookingEmailRequest request) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getEmail());
            helper.setSubject("Booking Confirmed - SkyBooker");

            // ✅ THYMELEAF CONTEXT
            Context context = buildBookingContext(request);

            // ✅ EMAIL TEMPLATE
            String emailHtml =
                    templateEngine.process("booking-confirmation", context);

            helper.setText(emailHtml, true);

            // ✅ PDF TEMPLATE
            String pdfHtml =
                    templateEngine.process("ticket", context);

            byte[] pdfBytes =
                    PdfGenerator.generatePdf(pdfHtml);

            helper.addAttachment(
                    "ticket.pdf",
                    new ByteArrayResource(pdfBytes)
            );

            // ✅ SEND EMAIL
            mailSender.send(message);

            // ✅ SAVE NOTIFICATION
            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    buildBookingMessage(request),
                    NotificationType.BOOKING_CONFIRMATION,
                    true,
                    request
            );

        } catch (Exception e) {

            log.error(
                    "Booking email failed for userId={}",
                    request.getUserId(),
                    e
            );

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    "Booking email failed",
                    NotificationType.BOOKING_CONFIRMATION,
                    false,
                    request
            );
        }
    }

    // ================= BULK EMAIL =================

    @Async
    @Override
    public void sendBulkEmail(BulkEmailRequest request) {

        if (request.getEmails() == null ||
                request.getEmails().isEmpty()) {

            log.error("Bulk email request has no recipients");
            return;
        }

        for (String email : request.getEmails()) {

            if (email == null || email.isBlank()) {
                log.warn("Skipping invalid email");
                continue;
            }

            try {

                MimeMessage message =
                        mailSender.createMimeMessage();

                MimeMessageHelper helper =
                        new MimeMessageHelper(message, true);

                helper.setFrom(fromEmail);
                helper.setTo(email);
                helper.setSubject(request.getSubject());

                Context context = new Context();

                context.setVariable(
                        "message",
                        request.getMessage()
                );

                String html =
                        templateEngine.process(
                                "bulk-email",
                                context
                        );

                helper.setText(html, true);

                mailSender.send(message);

                saveNotification(
                        null,
                        email,
                        buildBulkMessage(request),
                        NotificationType.BULK,
                        true,
                        request
                );

            } catch (Exception e) {

                log.error(
                        "Bulk email failed for {}",
                        email,
                        e
                );

                saveNotification(
                        null,
                        email,
                        "Bulk email failed",
                        NotificationType.BULK,
                        false,
                        request
                );
            }
        }
    }

    // ================= PAYMENT =================

    @Async
    @Override
    public void sendPaymentNotification(
            PaymentEmailRequest request) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getEmail());

            boolean success =
                    "SUCCESS".equalsIgnoreCase(
                            request.getStatus()
                    );

            helper.setSubject(
                    success
                            ? "Payment Successful - SkyBooker"
                            : "Payment Failed - SkyBooker"
            );

            Context context = new Context();

            context.setVariable(
                    "status",
                    request.getStatus()
            );

            context.setVariable(
                    "amount",
                    request.getAmount()
            );

            context.setVariable(
                    "userId",
                    request.getUserId()
            );

            String html =
                    templateEngine.process(
                            "payment",
                            context
                    );

            helper.setText(html, true);

            mailSender.send(message);

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    buildPaymentMessage(request),
                    success
                            ? NotificationType.PAYMENT_SUCCESS
                            : NotificationType.PAYMENT_FAILED,
                    true,
                    request
            );

        } catch (Exception e) {

            log.error(
                    "Payment email failed for userId={}",
                    request.getUserId(),
                    e
            );

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    "Payment email failed",
                    "SUCCESS".equalsIgnoreCase(
                            request.getStatus()
                    )
                            ? NotificationType.PAYMENT_SUCCESS
                            : NotificationType.PAYMENT_FAILED,
                    false,
                    request
            );
        }
    }

    // ================= CANCELLATION =================

    @Async
    @Override
    public void sendCancellationNotification(
            CancellationEmailRequest request) {

        try {

            if (request.getEmail() == null ||
                    request.getEmail().isBlank()) {

                log.error(
                        "Invalid email for cancellation: userId={}",
                        request.getUserId()
                );

                return;
            }

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getEmail());

            helper.setSubject(
                    "Booking Cancelled - SkyBooker"
            );

            Context context = new Context();

            context.setVariable(
                    "pnr",
                    request.getPnr()
            );

            context.setVariable(
                    "refundAmount",
                    request.getRefundAmount()
            );

            String html =
                    templateEngine.process(
                            "cancellation",
                            context
                    );

            helper.setText(html, true);

            mailSender.send(message);

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    buildCancellationMessage(request),
                    NotificationType.CANCELLATION,
                    true,
                    request
            );

        } catch (Exception e) {

            log.error(
                    "Cancellation email failed for userId={}",
                    request.getUserId(),
                    e
            );

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    "Cancellation email failed",
                    NotificationType.CANCELLATION,
                    false,
                    request
            );
        }
    }

    // ================= MESSAGE BUILDERS =================

    private String buildBookingMessage(
            BookingEmailRequest req) {

        return "Booking confirmed | PNR: " +
                req.getPnr() +
                " | Flight: " +
                req.getFlightNumber() +
                " | Fare: ₹" +
                req.getTotalFare();
    }

    private String buildPaymentMessage(
            PaymentEmailRequest req) {

        return "Payment " +
                req.getStatus() +
                " | Amount: ₹" +
                req.getAmount();
    }

    private String buildCancellationMessage(
            CancellationEmailRequest req) {

        return "Booking cancelled | PNR: " +
                req.getPnr() +
                " | Refund: ₹" +
                req.getRefundAmount();
    }

    private String buildBulkMessage(
            BulkEmailRequest req) {

        return "Bulk email | Subject: " +
                req.getSubject();
    }

    // ================= COMMON =================

    private Context buildBookingContext(
            BookingEmailRequest request) {

        Context context = new Context();

        // ✅ PASSENGER + SEAT TOGETHER
        context.setVariable(
                "passengerDetails",
                request.getPassengerDetails()
        );

        context.setVariable(
                "pnr",
                request.getPnr()
        );

        context.setVariable(
                "flightNumber",
                request.getFlightNumber()
        );

        context.setVariable(
                "departure",
                request.getDeparture()
        );

        context.setVariable(
                "arrival",
                request.getArrival()
        );

        context.setVariable(
                "totalFare",
                request.getTotalFare()
        );

        return context;
    }

    private void saveNotification(
            UUID userId,
            String email,
            String message,
            NotificationType type,
            boolean success,
            Object metadataObj) {

        String metadataJson = "{}";

        try {

            if (metadataObj != null) {

                metadataJson =
                        objectMapper.writeValueAsString(
                                metadataObj
                        );
            }

        } catch (JsonProcessingException e) {

            log.error(
                    "Metadata conversion failed",
                    e
            );
        }

        Notification notification =
                Notification.builder()
                        .userId(userId)
                        .email(email)
                        .type(type)
                        .message(message)
                        .metadata(metadataJson)
                        .createdAt(LocalDateTime.now())
                        .status(
                                success
                                        ? NotificationStatus.SENT
                                        : NotificationStatus.FAILED
                        )
                        .sentAt(
                                success
                                        ? LocalDateTime.now()
                                        : null
                        )
                        .build();

        repository.save(notification);
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private NotificationResponse mapToResponse(
            Notification n) {

        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .email(n.getEmail())
                .message(n.getMessage())
                .type(
                        n.getType() != null
                                ? n.getType().name()
                                : null
                )
                .status(
                        n.getStatus() != null
                                ? n.getStatus().name()
                                : null
                )
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}