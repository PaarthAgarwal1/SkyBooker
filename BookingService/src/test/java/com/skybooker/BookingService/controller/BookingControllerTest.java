package com.skybooker.BookingService.controller;

import com.skybooker.BookingService.dto.response.BookingResponse;
import com.skybooker.BookingService.security.JwtFilter;
import com.skybooker.BookingService.service.BookingService;
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

@WebMvcTest(controllers = BookingController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void getByIdReturnsBooking() throws Exception {
        UUID id = UUID.randomUUID();
        BookingResponse response = BookingResponse.builder()
                .id(id)
                .pnr("ABC123")
                .passenger(List.of("Aman Sharma"))
                .route("DEL -> BOM")
                .amount(5000)
                .status("CONFIRMED")
                .build();

        when(bookingService.getBookingById(id)).thenReturn(response);

        mockMvc.perform(get("/bookings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pnr").value("ABC123"));
    }
}
