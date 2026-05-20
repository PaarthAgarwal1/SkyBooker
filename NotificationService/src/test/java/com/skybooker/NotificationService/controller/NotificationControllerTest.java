package com.skybooker.NotificationService.controller;

import com.skybooker.NotificationService.dto.NotificationResponse;
import com.skybooker.NotificationService.security.JwtFilter;
import com.skybooker.NotificationService.security.ServiceAuthFilter;
import com.skybooker.NotificationService.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtFilter.class, ServiceAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getAllNotificationsReturnsList() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .notificationId(UUID.randomUUID())
                .email("user@test.com")
                .message("Payment successful")
                .status("SENT")
                .build();

        when(notificationService.getAllNotifications()).thenReturn(List.of(response));

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Payment successful"));
    }
}
