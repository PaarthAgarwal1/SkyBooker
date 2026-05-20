package com.skybooker.FlightService.repository;

import com.skybooker.FlightService.entity.Flight;
import com.skybooker.FlightService.entity.FlightStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class FlightRepositoryTest {

    @Autowired
    private FlightRepository flightRepository;

    @Test
    void saveAndFindByFlightNumber() {
        Flight flight = Flight.builder()
                .flightNumber("SB101")
                .status(FlightStatus.ON_TIME)
                .availableSeats(100)
                .build();

        flightRepository.save(flight);

        assertTrue(flightRepository.findByFlightNumber("SB101").isPresent());
        assertEquals(100, flightRepository.findByFlightNumber("SB101").get().getAvailableSeats());
    }
}
