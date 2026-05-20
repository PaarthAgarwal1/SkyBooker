package com.skybooker.SeatService.repository;

import com.skybooker.SeatService.entity.Seat;
import com.skybooker.SeatService.entity.SeatClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;

    @Test
    void saveAndFindByFlightIdAndSeatNumber() {
        UUID flightId = UUID.randomUUID();
        Seat seat = Seat.builder()
                .flightId(flightId)
                .seatNumber("1A")
                .seatClass(SeatClass.ECONOMY)
                .priceMultiplier(1.0)
                .build();

        seatRepository.save(seat);

        assertEquals("1A", seatRepository.findByFlightIdAndSeatNumber(flightId, "1A").getSeatNumber());
    }
}
