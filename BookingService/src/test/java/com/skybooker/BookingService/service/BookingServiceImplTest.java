package com.skybooker.BookingService.service;

import com.skybooker.BookingService.dto.response.FlightFeignResponse;
import com.skybooker.BookingService.dto.response.PassengerFeignResponse;
import com.skybooker.BookingService.entity.Booking;
import com.skybooker.BookingService.entity.BookingStatus;
import com.skybooker.BookingService.entity.TripType;
import com.skybooker.BookingService.feign.*;
import com.skybooker.BookingService.repository.BookingRepository;
import com.skybooker.BookingService.service.impl.BookingServiceImpl;
import com.skybooker.BookingService.util.PnrGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightClient flightClient;
    @Mock private SeatClient seatClient;
    @Mock private PassengerClient passengerClient;
    @Mock private NotificationClient notificationClient;
    @Mock private PaymentClient paymentClient;
    @Mock private PnrGenerator pnrGenerator;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void getBookingByIdReturnsBooking() {
        UUID id = UUID.randomUUID();
        UUID flightId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .bookingId(id)
                .userId(UUID.randomUUID())
                .flightId(flightId)
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
        FlightFeignResponse flight = new FlightFeignResponse();
        flight.setOriginAirportCode("DEL");
        flight.setDestinationAirportCode("BOM");
        PassengerFeignResponse passenger = new PassengerFeignResponse();
        passenger.setFirstName("Aman");
        passenger.setLastName("Sharma");

        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));
        when(flightClient.getFlight(flightId)).thenReturn(flight);
        when(passengerClient.getPassengersByBookingId(id)).thenReturn(List.of(passenger));

        assertEquals("ABC123", bookingService.getBookingById(id).getPnr());
    }
}
