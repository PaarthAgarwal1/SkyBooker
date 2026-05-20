package com.skybooker.PassengerService.service;

import com.skybooker.PassengerService.entity.Passenger;
import com.skybooker.PassengerService.entity.PassengerType;
import com.skybooker.PassengerService.repository.PassengerRepository;
import com.skybooker.PassengerService.service.impl.PassengerServiceImpl;
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
class PassengerServiceImplTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    @Test
    void getPassengerByIdReturnsPassenger() {
        UUID id = UUID.randomUUID();
        Passenger passenger = Passenger.builder()
                .passengerId(id)
                .firstName("Aman")
                .lastName("Sharma")
                .passengerType(PassengerType.ADULT)
                .build();

        when(passengerRepository.findById(id)).thenReturn(Optional.of(passenger));

        assertEquals("Aman", passengerService.getPassengerById(id).getFirstName());
    }
}
