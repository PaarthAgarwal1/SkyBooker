package com.skybooker.NotificationService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skybooker.NotificationService.entity.Notification;
import com.skybooker.NotificationService.entity.NotificationStatus;
import com.skybooker.NotificationService.entity.NotificationType;
import com.skybooker.NotificationService.repository.NotificationRepository;
import com.skybooker.NotificationService.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void getAllNotificationsReturnsList() {
        Notification notification = Notification.builder()
                .email("user@test.com")
                .message("Payment successful")
                .type(NotificationType.PAYMENT_SUCCESS)
                .status(NotificationStatus.SENT)
                .build();

        when(notificationRepository.findAll()).thenReturn(List.of(notification));

        assertEquals("Payment successful", notificationService.getAllNotifications().get(0).getMessage());
    }
}
