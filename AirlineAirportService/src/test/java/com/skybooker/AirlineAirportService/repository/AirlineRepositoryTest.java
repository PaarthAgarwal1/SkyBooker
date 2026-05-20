package com.skybooker.AirlineAirportService.repository;

import com.skybooker.AirlineAirportService.entity.Airline;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AirlineRepositoryTest {

    @Autowired
    private AirlineRepository airlineRepository;

    @Test
    void saveAndFindByIataCode() {
        Airline airline = Airline.builder()
                .airlineName("Sky Air")
                .iataCode("SA")
                .country("India")
                .isActive(true)
                .build();

        airlineRepository.save(airline);

        assertTrue(airlineRepository.findByIataCode("SA").isPresent());
        assertEquals("Sky Air", airlineRepository.findByIataCode("SA").get().getAirlineName());
    }
}
