package com.skybooker.BookingService.service;

import com.skybooker.BookingService.dto.request.ConfirmBookingRequest;
import com.skybooker.BookingService.dto.request.CreateBookingRequest;
import com.skybooker.BookingService.dto.response.BookingDetailResponse;
import com.skybooker.BookingService.dto.response.BookingResponse;
import com.skybooker.BookingService.dto.response.FareResponse;
import com.skybooker.BookingService.dto.response.FlightFeignResponse;
import com.skybooker.BookingService.entity.BookingStatus;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse confirmBooking(UUID bookingId, ConfirmBookingRequest request);

    BookingResponse getBookingById(UUID id);

    BookingResponse getBookingByPnr(String pnr);

    List<BookingResponse> getBookingByUser(UUID userId);

    List<BookingResponse> getBookingByFlight(UUID flightId);

    void cancelBooking(UUID bookingId);

    void updateStatus(UUID bookingId, BookingStatus status);

    FareResponse calculateFare(CreateBookingRequest request, FlightFeignResponse flight);

    void addAddOn(UUID bookingId,String meal,int luggageKg);

    String generatePnr();

    List<BookingResponse> getUpcomingBookings(UUID userId);

    FareResponse calculateFarePreview(CreateBookingRequest request);

    List<BookingResponse> getAllBookings();

    public BookingDetailResponse getBookingDetails(UUID bookingId);

    BookingStatus getBookingStatus(UUID id);
}
