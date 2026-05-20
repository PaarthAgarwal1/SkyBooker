package com.skybooker.NotificationService.repository;

import com.skybooker.NotificationService.entity.Notification;
import com.skybooker.NotificationService.entity.NotificationStatus;
import com.skybooker.NotificationService.entity.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void saveAndFindByUserId() {
        UUID userId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .userId(userId)
                .email("user@test.com")
                .message("Payment successful")
                .type(NotificationType.PAYMENT_SUCCESS)
                .status(NotificationStatus.SENT)
                .build();

        notificationRepository.save(notification);

        assertEquals(1, notificationRepository.findByUserId(userId).size());
        assertEquals("Payment successful", notificationRepository.findByUserId(userId).get(0).getMessage());
    }
}
