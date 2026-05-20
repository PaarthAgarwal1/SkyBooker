package com.skybooker.PassengerService.controller;

import com.skybooker.PassengerService.dto.response.PassengerResponse;
import com.skybooker.PassengerService.security.JwtFilter;
import com.skybooker.PassengerService.service.PassengerService;
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

@WebMvcTest(controllers = PassengerController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class PassengerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PassengerService passengerService;

    @Test
    void getPassengerByIdReturnsPassenger() throws Exception {
        UUID id = UUID.randomUUID();
        PassengerResponse response = PassengerResponse.builder()
                .passengerId(id)
                .firstName("Aman")
                .lastName("Sharma")
                .build();

        when(passengerService.getPassengerById(id)).thenReturn(response);

        mockMvc.perform(get("/passengers/{passengerId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Aman"));
    }
}
