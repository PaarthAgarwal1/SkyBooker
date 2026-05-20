package com.skybooker.FlightService.controller;

import com.skybooker.FlightService.dto.response.FlightResponse;
import com.skybooker.FlightService.security.JwtFilter;
import com.skybooker.FlightService.service.FlightService;
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

@WebMvcTest(controllers = FlightController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightService flightService;

    @Test
    void getByIdReturnsFlight() throws Exception {
        UUID id = UUID.randomUUID();
        FlightResponse response = FlightResponse.builder()
                .flightId(id)
                .flightNumber("SB101")
                .originAirportCode("DEL")
                .destinationAirportCode("BOM")
                .build();

        when(flightService.getFlightById(id)).thenReturn(response);

        mockMvc.perform(get("/flights/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("SB101"));
    }
}
