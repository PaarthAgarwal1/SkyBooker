package com.skybooker.PaymentService.controller;

import com.skybooker.PaymentService.dto.response.PaymentResponse;
import com.skybooker.PaymentService.entity.PaymentStatus;
import com.skybooker.PaymentService.security.JwtFilter;
import com.skybooker.PaymentService.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void byBookingReturnsPayment() throws Exception {
        UUID bookingId = UUID.randomUUID();
        PaymentResponse response = PaymentResponse.builder()
                .bookingId(bookingId)
                .amount(5000.0)
                .status(PaymentStatus.PAID)
                .build();

        when(paymentService.getPaymentByBooking(bookingId)).thenReturn(response);

        mockMvc.perform(get("/payments/booking/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}
