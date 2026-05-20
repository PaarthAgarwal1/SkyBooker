package com.skybooker.SeatService.service;

import com.skybooker.SeatService.client.FlightClient;
import com.skybooker.SeatService.entity.Seat;
import com.skybooker.SeatService.entity.SeatClass;
import com.skybooker.SeatService.entity.SeatStatus;
import com.skybooker.SeatService.repository.SeatRepository;
import com.skybooker.SeatService.service.impl.SeatServiceImpl;
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
class SeatServiceImplTest {

    @Mock private SeatRepository seatRepository;
    @Mock private FlightClient flightClient;

    @InjectMocks
    private SeatServiceImpl seatService;

    @Test
    void getSeatByIdReturnsSeat() {
        UUID id = UUID.randomUUID();
        Seat seat = Seat.builder()
                .seatId(id)
                .seatNumber("1A")
                .seatClass(SeatClass.ECONOMY)
                .status(SeatStatus.AVAILABLE)
                .build();

        when(seatRepository.findById(id)).thenReturn(Optional.of(seat));

        assertEquals("1A", seatService.getSeatById(id).getSeatNumber());
    }
}
