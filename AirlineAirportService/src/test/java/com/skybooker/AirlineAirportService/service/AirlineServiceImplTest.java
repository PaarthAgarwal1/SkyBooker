package com.skybooker.AirlineAirportService.service;

import com.skybooker.AirlineAirportService.entity.Airline;
import com.skybooker.AirlineAirportService.repository.AirlineRepository;
import com.skybooker.AirlineAirportService.service.impl.AirlineServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirlineServiceImplTest {

    @Mock
    private AirlineRepository airlineRepository;

    @InjectMocks
    private AirlineServiceImpl airlineService;

    @Test
    void getAirlineByIdReturnsAirline() {
        UUID id = UUID.randomUUID();
        Airline airline = Airline.builder()
                .airlineId(id)
                .airlineName("Sky Air")
                .iataCode("SA")
                .country("India")
                .isActive(true)
                .build();

        when(airlineRepository.findById(id)).thenReturn(Optional.of(airline));

        assertEquals("Sky Air", airlineService.getAirlineById(id).getAirlineName());
    }
}
