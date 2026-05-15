package com.skybooker.NotificationService.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkEmailRequest {
    private List<String> emails;
    private String subject;
    private String message;

}
