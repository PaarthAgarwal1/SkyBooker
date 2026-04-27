package com.skybooker.BookingService.service.impl;

import com.skybooker.BookingService.dto.request.*;
import com.skybooker.BookingService.dto.response.*;
import com.skybooker.BookingService.entity.Booking;
import com.skybooker.BookingService.entity.BookingStatus;
import com.skybooker.BookingService.exception.BookingException;
import com.skybooker.BookingService.exception.ResourceNotFoundException;
import com.skybooker.BookingService.feign.*;
import com.skybooker.BookingService.repository.BookingRepository;
import com.skybooker.BookingService.service.BookingService;
import com.skybooker.BookingService.util.PnrGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;
    private final SeatClient seatClient;
    private final PassengerClient passengerClient;
    private final NotificationClient notificationClient;
    private final PnrGenerator pnrGenerator;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new BookingException("Seat selection required");
        }

        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            throw new BookingException("Passengers required");
        }

        if (request.getSeatIds().size() != request.getPassengers().size()) {
            throw new BookingException("Seat count must match passenger count");
        }

        FlightFeignResponse flight = flightClient.getFlight(request.getFlightId());

        if (flight == null) {
            throw new ResourceNotFoundException("Flight not found");
        }

        if (flight.getAvailableSeats() < request.getSeatIds().size()) {
            throw new BookingException("Not enough seats available");
        }

        List<UUID> heldSeats = new ArrayList<>();

        try {
            for (UUID seatId : request.getSeatIds()) {

                SeatFeignResponse seat = seatClient.holdSeat(seatId);

                if (seat == null) {
                    throw new BookingException("Seat not available: " + seatId);
                }

                heldSeats.add(seatId);
            }

        } catch (Exception e) {
            heldSeats.forEach(seatClient::releaseSeat);
            throw new BookingException("Seat hold failed");
        }

        FareResponse fare = calculateFare(request, flight);

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .flightId(request.getFlightId())
                .seatIds(request.getSeatIds())
                .pnrCode(generatePnr())
                .tripType(request.getTripType())
                .status(BookingStatus.PENDING)
                .totalFare(BigDecimal.valueOf(fare.getTotalFare()))
                .baseFare(BigDecimal.valueOf(fare.getBaseFare()))
                .taxes(BigDecimal.valueOf(fare.getTaxes()))
                .mealPreference(request.getMealPreference())
                .luggageKg(request.getLuggageKg())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .bookedAt(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        try {
            for (PassengerFeignRequest p : request.getPassengers()) {

                if (!request.getSeatIds().contains(p.getSeatId())) {
                    throw new BookingException("Invalid seat mapping");
                }

                PassengerFeignRequest passenger = new PassengerFeignRequest();
                passenger.setBookingId(booking.getBookingId());
                passenger.setFirstName(p.getFirstName());
                passenger.setLastName(p.getLastName());
                passenger.setGender(p.getGender());
                passenger.setDateOfBirth(p.getDateOfBirth());
                passenger.setPassportNumber(p.getPassportNumber());
                passenger.setNationality(p.getNationality());
                passenger.setSeatId(p.getSeatId());
                passenger.setSeatNumber(p.getSeatNumber());
                passenger.setPassengerType(p.getPassengerType());
                passenger.setPassportExpiry(p.getPassportExpiry());

                passengerClient.addPassenger(passenger);
            }

        } catch (Exception e) {
            heldSeats.forEach(seatClient::releaseSeat);
            booking.setStatus(BookingStatus.CANCELLED);
            throw new RuntimeException(e.getMessage());
//            throw new BookingException("Passenger creation failed");
        }

        boolean paymentSuccess = true;

        if (!paymentSuccess) {
            heldSeats.forEach(seatClient::releaseSeat);
            booking.setStatus(BookingStatus.CANCELLED);
            throw new BookingException("Payment failed");
        }

        heldSeats.forEach(seatClient::confirmSeat);

        booking.setStatus(BookingStatus.CONFIRMED);

        flightClient.decrementSeats(booking.getFlightId(), heldSeats.size());

        notificationClient.sendBooking(
                BookingEmailRequest.builder()
                        .email(booking.getContactEmail())
                        .pnr(booking.getPnrCode())
                        .build()
        );

        return mapToResponse(booking);
    }

    @Override
    public BookingResponse getBookingById(UUID id) {
        return mapToResponse(
                bookingRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"))
        );
    }

    @Override
    public BookingResponse getBookingByPnr(String pnr) {
        return mapToResponse(
                bookingRepository.findByPnrCode(pnr)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"))
        );
    }

    @Override
    public List<BookingResponse> getBookingByUser(UUID userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getBookingByFlight(UUID flightId) {
        return bookingRepository.findByFlightId(flightId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelBooking(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Only confirmed bookings can be cancelled");
        }

        booking.getSeatIds().forEach(seatClient::releaseSeat);

        flightClient.incrementSeats(
                booking.getFlightId(),
                booking.getSeatIds().size()
        );

        booking.setStatus(BookingStatus.CANCELLED);

        notificationClient.sendCancellation(
                CancellationEmailRequest.builder()
                        .email(booking.getContactEmail())
                        .pnr(booking.getPnrCode())
                        .build()
        );
    }

    @Override
    public void updateStatus(UUID bookingId, BookingStatus status) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(status);
    }

    @Override
    public FareResponse calculateFare(CreateBookingRequest request, FlightFeignResponse flight) {

        double base = flight.getBasePrice().doubleValue();
        double taxes = base * 0.18;

        double meal = "VEG".equalsIgnoreCase(request.getMealPreference()) ? 200 : 0;
        double luggage = request.getLuggageKg() * 50;

        double total = (base * request.getSeatIds().size()) + taxes + meal + luggage;

        return FareResponse.builder()
                .baseFare(base)
                .taxes(taxes)
                .totalFare(total)
                .build();
    }

    @Override
    public void addAddOn(UUID bookingId, String meal, int luggageKg) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setMealPreference(meal);
        booking.setLuggageKg(luggageKg);

        double extra = (luggageKg * 50) +
                ("VEG".equalsIgnoreCase(meal) ? 200 : 0);

        booking.setTotalFare(
                booking.getTotalFare().add(BigDecimal.valueOf(extra))
        );
    }

    @Override
    public String generatePnr() {

        String pnr;

        do {
            pnr = pnrGenerator.generate();
        } while (bookingRepository.findByPnrCode(pnr).isPresent());

        return pnr;
    }

    @Override
    public List<BookingResponse> getUpcomingBookings(UUID userId) {

        return bookingRepository
                .findByUserIdAndStatus(userId, BookingStatus.CONFIRMED)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public FareResponse calculateFarePreview(CreateBookingRequest request) {

        FlightFeignResponse flight = flightClient.getFlight(request.getFlightId());

        return calculateFare(request,flight);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse build = BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .pnrCode(booking.getPnrCode())
                .status(booking.getStatus().name())
                .totalFare(booking.getTotalFare().doubleValue())
                .build();
        return build;
    }
}