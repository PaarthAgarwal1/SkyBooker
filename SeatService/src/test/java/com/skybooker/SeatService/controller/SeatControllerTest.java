package com.skybooker.SeatService.controller;

import com.skybooker.SeatService.dto.response.SeatResponse;
import com.skybooker.SeatService.entity.SeatClass;
import com.skybooker.SeatService.entity.SeatStatus;
import com.skybooker.SeatService.security.JwtFilter;
import com.skybooker.SeatService.service.SeatService;
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

@WebMvcTest(controllers = SeatController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeatService seatService;

    @Test
    void getSeatByIdReturnsSeat() throws Exception {
        UUID id = UUID.randomUUID();
        SeatResponse response = SeatResponse.builder()
                .seatId(id)
                .seatNumber("1A")
                .seatClass(SeatClass.ECONOMY)
                .status(SeatStatus.AVAILABLE)
                .build();

        when(seatService.getSeatById(id)).thenReturn(response);

        mockMvc.perform(get("/seats/{seatId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatNumber").value("1A"));
    }
}
