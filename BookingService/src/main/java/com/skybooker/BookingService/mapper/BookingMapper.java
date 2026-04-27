package com.skybooker.BookingService.mapper;

import com.skybooker.BookingService.dto.response.BookingResponse;
import com.skybooker.BookingService.entity.Booking;

public class BookingMapper {
    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .pnrCode(booking.getPnrCode())
                .status(booking.getStatus().name())
                .totalFare(booking.getTotalFare().doubleValue())
                .build();
    }
}
