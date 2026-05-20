package com.skybooker.PassengerService.repository;

import com.skybooker.PassengerService.entity.Passenger;
import com.skybooker.PassengerService.entity.PassengerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class PassengerRepositoryTest {

    @Autowired
    private PassengerRepository passengerRepository;

    @Test
    void saveAndFindByPassportNumber() {
        Passenger passenger = Passenger.builder()
                .flightId(UUID.randomUUID())
                .firstName("Aman")
                .passportNumber("P123456")
                .passengerType(PassengerType.ADULT)
                .build();

        passengerRepository.save(passenger);

        assertEquals("Aman", passengerRepository.findByPassportNumber("P123456").getFirstName());
    }
}
