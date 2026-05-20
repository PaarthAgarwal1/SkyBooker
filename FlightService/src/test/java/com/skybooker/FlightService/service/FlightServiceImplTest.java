package com.skybooker.FlightService.service;

import com.skybooker.FlightService.client.AirlineClient;
import com.skybooker.FlightService.entity.Flight;
import com.skybooker.FlightService.entity.FlightStatus;
import com.skybooker.FlightService.repository.FlightRepository;
import com.skybooker.FlightService.service.impl.FlightServiceImpl;
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
class FlightServiceImplTest {

    @Mock private FlightRepository flightRepository;
    @Mock private AirlineClient airlineClient;

    @InjectMocks
    private FlightServiceImpl flightService;

    @Test
    void getFlightByIdReturnsFlight() {
        UUID id = UUID.randomUUID();
        Flight flight = Flight.builder()
                .flightId(id)
                .flightNumber("SB101")
                .status(FlightStatus.ON_TIME)
                .build();

        when(flightRepository.findById(id)).thenReturn(Optional.of(flight));

        assertEquals("SB101", flightService.getFlightById(id).getFlightNumber());
    }
}
