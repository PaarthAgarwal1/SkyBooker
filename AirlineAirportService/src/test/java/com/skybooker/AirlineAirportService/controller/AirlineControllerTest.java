package com.skybooker.AirlineAirportService.controller;

import com.skybooker.AirlineAirportService.dto.airline.AirlineResponse;
import com.skybooker.AirlineAirportService.security.JwtFilter;
import com.skybooker.AirlineAirportService.service.AirlineService;
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

@WebMvcTest(controllers = AirlineController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class AirlineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AirlineService airlineService;

    @Test
    void getByIdReturnsAirline() throws Exception {
        UUID id = UUID.randomUUID();
        AirlineResponse response = AirlineResponse.builder()
                .airlineId(id)
                .airlineName("Sky Air")
                .iataCode("SA")
                .country("India")
                .isActive(true)
                .build();

        when(airlineService.getAirlineById(id)).thenReturn(response);

        mockMvc.perform(get("/airline/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.airlineName").value("Sky Air"))
                .andExpect(jsonPath("$.iataCode").value("SA"));
    }
}
