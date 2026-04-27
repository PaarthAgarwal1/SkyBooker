package com.skybooker.NotificationService.service.impl;

import com.skybooker.NotificationService.dto.BookingEmailRequest;
import com.skybooker.NotificationService.dto.BulkEmailRequest;
import com.skybooker.NotificationService.dto.CancellationEmailRequest;
import com.skybooker.NotificationService.dto.PaymentEmailRequest;
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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository repository;
    private final TemplateEngine templateEngine;

    public NotificationServiceImpl(NotificationRepository repository, JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendBookingConfirmation(BookingEmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getEmail());
            helper.setSubject("Booking Confirmed - SkyBooker");

            // ✅ Create Thymeleaf context
            Context context = new Context();
            context.setVariable("passengerName", request.getPassengerName());
            context.setVariable("pnr", request.getPnr());
            context.setVariable("flightNumber", request.getFlightNumber());
            context.setVariable("departure", request.getDeparture());
            context.setVariable("arrival", request.getArrival());
            context.setVariable("seatNumber", request.getSeatNumber());
            context.setVariable("totalFare", request.getTotalFare());

            // ✅ 1. Email HTML
            String emailHtml = templateEngine.process("booking-confirmation", context);
            helper.setText(emailHtml, true);

            // ✅ 2. PDF HTML (can use SAME template or different one)
            String pdfHtml = templateEngine.process("ticket", context);

            // ✅ 3. Generate PDF from HTML
            byte[] pdfBytes = PdfGenerator.generatePdf(pdfHtml);

            // ✅ 4. Attach PDF
            helper.addAttachment("ticket.pdf", new ByteArrayResource(pdfBytes));

            // ✅ Send mail
            mailSender.send(message);

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    emailHtml,
                    NotificationType.BOOKING_CONFIRMATION,
                    true
            );

        } catch (Exception e) {
            log.error("Booking email failed", e); // 🔥 VERY IMPORTANT

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    "Booking email failed",
                    NotificationType.BOOKING_CONFIRMATION,
                    false
            );
        }
    }

    @Async
    @Override
    public void sendBulkEmail(BulkEmailRequest request) {

        if (request.getEmails() == null || request.getEmails().isEmpty()) {
            log.error("Email is null, cannot send cancellation email: {}", request);
            return;
        }

        for (String email : request.getEmails()) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom(fromEmail);
                helper.setTo(email);
                helper.setSubject(request.getSubject());

                Context context = new Context();
                context.setVariable("message", request.getMessage());

                String html = templateEngine.process("bulk-email", context);

                helper.setText(html, true);

                mailSender.send(message);

            } catch (Exception e) {
                log.error("Failed for {}: {}", email, e.getMessage());
            }
        }
    }

    @Async
    @Override
    public void sendPaymentNotification(PaymentEmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getEmail());

            boolean success = request.getStatus().equalsIgnoreCase("SUCCESS");

            String subject = success ?
                    "Payment Successful - SkyBooker" :
                    "Payment Failed - SkyBooker";

            helper.setSubject(subject);

            // ✅ Thymeleaf context
            Context context = new Context();
            context.setVariable("status", request.getStatus());
            context.setVariable("amount", request.getAmount());
            context.setVariable("userId", request.getUserId());

            // ✅ HTML email only
            String html = templateEngine.process("payment", context);
            helper.setText(html, true);

            mailSender.send(message);

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    html,
                    success ? NotificationType.PAYMENT_SUCCESS : NotificationType.PAYMENT_FAILED,
                    true
            );

        } catch (Exception e) {
            log.error("Payment email failed", e);

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    "Payment email failed",
                    request.getStatus().equalsIgnoreCase("SUCCESS") ?
                            NotificationType.PAYMENT_SUCCESS :
                            NotificationType.PAYMENT_FAILED,
                    false
            );
        }
    }

    @Async
    @Override
    public void sendCancellationNotification(CancellationEmailRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                log.error("Email is null, cannot send cancellation email: {}", request);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getEmail());
            helper.setSubject("Booking Cancelled - SkyBooker");

            // ✅ Thymeleaf context
            Context context = new Context();
            context.setVariable("pnr", request.getPnr());
            context.setVariable("refundAmount", request.getRefundAmount());

            // ✅ HTML email only
            String html = templateEngine.process("cancellation", context);
            helper.setText(html, true);

            mailSender.send(message);

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    html,
                    NotificationType.CANCELLATION,
                    true
            );

        } catch (Exception e) {
            log.error("Cancellation email failed", e);

            saveNotification(
                    request.getUserId(),
                    request.getEmail(),
                    "Cancellation email failed",
                    NotificationType.CANCELLATION,
                    false
            );
        }
    }

    private void sendEmail(String to, String subject , String body, UUID userId, NotificationType type){
        Notification notification =Notification.builder()
                .userId(userId)
                .email(to)
                .type(type)
                .message(body)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            SimpleMailMessage message=new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        }catch (Exception e){
            notification.setStatus(NotificationStatus.FAILED);
        }
        repository.save(notification);
    }

    private String buildBookingEmail(BookingEmailRequest req ){
        return """
                ✈️ Booking Confirmed!

                Passenger: %s
                PNR: %s
                Flight: %s

                From: %s
                To: %s

                Seat: %s
                Total Fare: ₹%s

                Thank you for choosing SkyBooker!
                """.formatted(
                req.getPassengerName(),
                req.getPnr(),
                req.getFlightNumber(),
                req.getDeparture(),
                req.getArrival(),
                req.getSeatNumber(),
                req.getTotalFare()
        );
    }

    private String generateHtml(BookingEmailRequest req) {

        Context context = new Context();
        context.setVariable("passengerName", req.getPassengerName());
        context.setVariable("pnr", req.getPnr());
        context.setVariable("flightNumber", req.getFlightNumber());
        context.setVariable("departure", req.getDeparture());
        context.setVariable("arrival", req.getArrival());
        context.setVariable("seatNumber", req.getSeatNumber());
        context.setVariable("totalFare", req.getTotalFare());

        return templateEngine.process("booking-confirmation", context);
    }
    private void saveNotification(UUID userId, String email,
                                  String message,
                                  NotificationType type,
                                  boolean success) {

        Notification notification = Notification.builder()
                .userId(userId)
                .email(email)
                .type(type)
                .message(message)
                .createdAt(LocalDateTime.now())
                .status(success ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .sentAt(success ? LocalDateTime.now() : null)
                .build();

        repository.save(notification);
    }
}
