package com.skybooker.BookingService.repository;

import com.skybooker.BookingService.entity.Booking;
import com.skybooker.BookingService.entity.BookingStatus;
import com.skybooker.BookingService.entity.TripType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void saveAndFindByPnrCode() {
        Booking booking = Booking.builder()
                .userId(UUID.randomUUID())
                .flightId(UUID.randomUUID())
                .pnrCode("ABC123")
                .tripType(TripType.ONE_WAY)
                .status(BookingStatus.CONFIRMED)
                .seatIds(List.of(UUID.randomUUID()))
                .totalFare(BigDecimal.valueOf(5000))
                .baseFare(BigDecimal.valueOf(4500))
                .taxes(BigDecimal.valueOf(500))
                .contactEmail("user@test.com")
                .contactPhone("9999999999")
                .idempotencyKey("key-1")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        bookingRepository.save(booking);

        assertTrue(bookingRepository.findByPnrCode("ABC123").isPresent());
        assertEquals(BookingStatus.CONFIRMED, bookingRepository.findByPnrCode("ABC123").get().getStatus());
    }
}
