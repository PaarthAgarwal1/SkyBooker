package com.skybooker.NotificationService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID notificationId;

    private String email;           // null → means broadcast

    private String message;

    private String type;            // enum → string for frontend

    private String status;          // enum → string

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
}