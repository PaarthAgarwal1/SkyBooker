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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;
    private final SeatClient seatClient;
    private final PassengerClient passengerClient;
    private final NotificationClient notificationClient;
    private final PaymentClient paymentClient;
    private final PnrGenerator pnrGenerator;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        log.info("Creating booking for userId: {}", request.getUserId());

        validateRequest(request);

        if (request.getIdempotencyKey() != null) {

            Optional<Booking> existing =
                    bookingRepository.findByIdempotencyKey(
                            request.getIdempotencyKey()
                    );

            if (existing.isPresent()) {

                log.warn("Duplicate booking request detected with idempotencyKey: {}",
                        request.getIdempotencyKey());

                return mapToResponse(existing.get());
            }
        }

        FlightFeignResponse flight =
                flightClient.getFlight(request.getFlightId());

        if (flight == null) {

            log.error("Flight not found: {}", request.getFlightId());

            throw new ResourceNotFoundException("Flight not found");
        }

        log.info("Fetched flight details for flightId: {}",
                request.getFlightId());

        if (flight.getAvailableSeats() < request.getSeatIds().size()) {

            log.warn("Not enough seats available for flightId: {}",
                    request.getFlightId());

            throw new BookingException("Not enough seats available");
        }

        List<UUID> heldSeats = new ArrayList<>();

        try {

            log.info("Holding seats: {}", request.getSeatIds());

            for (UUID seatId : request.getSeatIds()) {

                SeatFeignResponse seat =
                        seatClient.holdSeat(seatId);

                if (seat == null) {

                    log.error("Seat not available: {}", seatId);

                    throw new BookingException(
                            "Seat not available: " + seatId
                    );
                }

                heldSeats.add(seatId);
            }

            log.info("Seats held successfully: {}", heldSeats);

        } catch (Exception e) {

            log.error("Seat hold failed", e);

            heldSeats.forEach(seatClient::releaseSeat);

            throw new BookingException(
                    "Seat hold failed: " + e.getMessage()
            );
        }

        FareResponse fare = calculateFare(request, flight);

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .flightId(request.getFlightId())
                .seatIds(request.getSeatIds())
                .pnrCode(generatePnr())
                .tripType(request.getTripType())
                .status(BookingStatus.PAYMENT_PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .expiryTime(LocalDateTime.now().plusMinutes(5))
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

        log.info("Booking created successfully with bookingId: {}",
                booking.getBookingId());

        try {

            for (AddPassengerRequest p : request.getPassengers()) {

                if (!request.getSeatIds().contains(p.getSeatId())) {

                    log.error("Invalid seat mapping for passenger");

                    throw new BookingException("Invalid seat mapping");
                }

                PassengerFeignRequest passenger =
                        PassengerFeignRequest.builder()
                                .bookingId(booking.getBookingId())
                                .flightId(booking.getFlightId())
                                .title(p.getTitle())
                                .firstName(p.getFirstName())
                                .lastName(p.getLastName())
                                .gender(p.getGender())
                                .dateOfBirth(p.getDateOfBirth())
                                .passportNumber(p.getPassportNumber())
                                .nationality(p.getNationality())
                                .seatId(p.getSeatId())
                                .seatNumber(p.getSeatNumber())
                                .passengerType(p.getPassengerType())
                                .passportExpiry(p.getPassportExpiry())
                                .build();

                passengerClient.addPassenger(passenger);
            }

            log.info("Passengers registered successfully for bookingId: {}",
                    booking.getBookingId());

        } catch (Exception e) {

            log.error("Passenger creation failed", e);

            heldSeats.forEach(seatClient::releaseSeat);

            booking.setStatus(BookingStatus.CANCELLED);

            bookingRepository.save(booking);

            throw new BookingException(
                    "Passenger creation failed: " + e.getMessage()
            );
        }

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(
            UUID bookingId,
            ConfirmBookingRequest request
    ) {

        log.info("Confirming booking with bookingId: {}", bookingId);

        log.info("Booking confirm request received: {}", request);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found in confirmBooking: {}", bookingId);
                    return new ResourceNotFoundException(
                            "Booking not found"
                    );
                });

        log.info("Booking status before confirmation: {}",
                booking.getStatus());

        if (booking.getStatus() == BookingStatus.CONFIRMED) {

            log.warn("Booking already confirmed: {}", bookingId);

            return mapToResponse(booking);
        }

        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {

            log.warn("Invalid booking state for bookingId {}: {}",
                    bookingId,
                    booking.getStatus());

            throw new BookingException(
                    "Invalid booking state" + booking.getStatus()
            );
        }

        PaymentStatusResponse paymentStatus =
                paymentClient.getPaymentStatus(
                        request.getPaymentId()
                );

        log.info("Payment status received: {}", paymentStatus);

        if (paymentStatus == null ||
                paymentStatus.getStatus() != PaymentStatus.PAID) {

            log.warn("Payment verification failed for bookingId: {}",
                    bookingId);

            booking.getSeatIds().forEach(seatClient::releaseSeat);

            booking.setStatus(BookingStatus.CANCELLED);

            bookingRepository.save(booking);

            return mapToResponse(booking);
        }

        booking.getSeatIds().forEach(seatClient::confirmSeat);

        log.info("Seats confirmed for bookingId: {}", bookingId);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentId(request.getPaymentId());

        flightClient.decrementSeats(
                booking.getFlightId(),
                booking.getSeatIds().size()
        );

        FlightFeignResponse flight =
                flightClient.getFlight(booking.getFlightId());

        List<PassengerFeignResponse> passengers =
                passengerClient.getPassengersByBookingId(
                        booking.getBookingId()
                );

        List<String> passengerDetails = passengers.stream()
                .map(p ->
                        p.getFirstName() + " " +
                                p.getLastName() +
                                " - Seat " +
                                p.getSeatNumber()
                )
                .toList();

        notificationClient.sendBooking(
                BookingEmailRequest.builder()
                        .userId(booking.getUserId())
                        .email(booking.getContactEmail())
                        .passengerDetails(passengerDetails)
                        .pnr(booking.getPnrCode())
                        .flightNumber(flight.getFlightNumber())
                        .departure(flight.getDepartureTime().toString())
                        .arrival(flight.getArrivalTime().toString())
                        .totalFare(booking.getTotalFare().doubleValue())
                        .build()
        );

        log.info("Booking notification sent to: {}",
                booking.getContactEmail());

        log.info("Booking confirmed successfully. BookingId: {}",
                bookingId);

        bookingRepository.save(booking);

        return mapToResponse(booking);
    }

    @Override
    public BookingResponse getBookingById(UUID id) {

        log.info("Fetching booking by ID: {}", id);

        return mapToResponse(
                bookingRepository.findById(id)
                        .orElseThrow(() -> {
                            log.error("Booking not found in getBookingById: {}", id);
                            return new ResourceNotFoundException(
                                    "Booking not found"
                            );
                        })
        );
    }

    @Override
    public BookingResponse getBookingByPnr(String pnr) {

        log.info("Fetching booking by PNR: {}", pnr);

        return mapToResponse(
                bookingRepository.findByPnrCode(pnr)
                        .orElseThrow(() -> {
                            log.error("Booking not found for PNR: {}", pnr);
                            return new ResourceNotFoundException(
                                    "Booking not found"
                            );
                        })
        );
    }

    @Override
    public BookingStatus getBookingStatus(UUID id) {

        log.info("Fetching booking status for bookingId: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found in getBookingStatus: {}", id);
                    return new RuntimeException("Booking not found");
                });

        return booking.getStatus();
    }

    @Override
    public List<BookingResponse> getBookingByUser(UUID userId) {

        log.info("Fetching bookings for userId: {}", userId);

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getBookingByFlight(UUID flightId) {

        log.info("Fetching bookings for flightId: {}", flightId);

        return bookingRepository.findByFlightId(flightId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelBooking(UUID bookingId) {

        log.info("Cancelling booking with bookingId: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found in cancelBooking: {}", bookingId);
                    return new ResourceNotFoundException(
                            "Booking not found"
                    );
                });

        if (booking.getStatus() != BookingStatus.CONFIRMED) {

            log.warn("Attempted cancellation of non-confirmed booking: {}",
                    bookingId);

            throw new BookingException(
                    "Only confirmed bookings can be cancelled"
            );
        }

        booking.getSeatIds().forEach(seatClient::releaseSeat);

        RefundRequest refundRequest =
                RefundRequest.builder()
                        .paymentId(booking.getPaymentId())
                        .refundAmount(
                                booking.getTotalFare().doubleValue()
                        )
                        .build();

        paymentClient.refund(refundRequest);

        log.info("Refund initiated for paymentId: {}",
                booking.getPaymentId());

        flightClient.incrementSeats(
                booking.getFlightId(),
                booking.getSeatIds().size()
        );

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        notificationClient.sendCancellation(
                CancellationEmailRequest.builder()
                        .email(booking.getContactEmail())
                        .pnr(booking.getPnrCode())
                        .build()
        );

        log.info("Booking cancelled successfully: {}", bookingId);
    }

    @Override
    public void updateStatus(UUID bookingId,
                             BookingStatus status) {

        log.info("Updating booking status. BookingId: {}, Status: {}",
                bookingId,
                status);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found in updateStatus: {}", bookingId);
                    return new ResourceNotFoundException(
                            "Booking not found"
                    );
                });

        booking.setStatus(status);

        bookingRepository.save(booking);
    }

    @Override
    public FareResponse calculateFare(
            CreateBookingRequest request,
            FlightFeignResponse flight
    ) {

        log.info("Calculating fare for flightId: {}",
                request.getFlightId());

        int passengerCount = request.getPassengers().size();

        double baseFarePerPassenger =
                flight.getBasePrice().doubleValue();

        double baseFare =
                baseFarePerPassenger * passengerCount;

        double seatCharges = 0;

        for (UUID seatId : request.getSeatIds()) {

            SeatFeignResponse seat =
                    seatClient.getSeat(seatId);

            if (seat == null) {

                log.error("Seat not found: {}", seatId);

                throw new BookingException(
                        "Seat not found: " + seatId
                );
            }

            double extraMultiplier =
                    seat.getPriceMultiplier() - 1;

            if (extraMultiplier > 0) {

                seatCharges +=
                        baseFarePerPassenger * extraMultiplier;
            }
        }

        double mealCharges = 0;

        if (request.getMealPreference() != null) {

            switch (request.getMealPreference().toUpperCase()) {

                case "VEG" ->
                        mealCharges = 250 * passengerCount;

                case "NONVEG" ->
                        mealCharges = 400 * passengerCount;

                case "VEGAN" ->
                        mealCharges = 350 * passengerCount;

                case "JAIN" ->
                        mealCharges = 300 * passengerCount;

                default ->
                        mealCharges = 0;
            }
        }

        double baggageCharges = 0;

        int freeLimit = 15;

        if (request.getLuggageKg() > freeLimit) {

            int extraKg =
                    request.getLuggageKg() - freeLimit;

            baggageCharges = extraKg * 80;
        }

        double fuelSurcharge = 1200;

        double convenienceFee = 250;

        double subtotal =
                baseFare +
                        seatCharges +
                        mealCharges +
                        baggageCharges +
                        fuelSurcharge +
                        convenienceFee;

        double taxes = subtotal * 0.05;

        double totalFare = subtotal + taxes;

        log.info("Fare calculated successfully. Total Fare: {}",
                totalFare);

        return FareResponse.builder()
                .baseFare(round(baseFare))
                .seatCharges(round(seatCharges))
                .mealCharges(round(mealCharges))
                .baggageCharges(round(baggageCharges))
                .fuelSurcharge(round(fuelSurcharge))
                .convenienceFee(round(convenienceFee))
                .taxes(round(taxes))
                .totalFare(round(totalFare))
                .build();
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public void addAddOn(
            UUID bookingId,
            String meal,
            int luggageKg
    ) {

        log.info("Adding add-ons for bookingId: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found in addAddOn: {}", bookingId);
                    return new ResourceNotFoundException(
                            "Booking not found"
                    );
                });

        booking.setMealPreference(meal);
        booking.setLuggageKg(luggageKg);

        double extra =
                (luggageKg * 50) +
                        ("VEG".equalsIgnoreCase(meal)
                                ? 200
                                : 0);

        booking.setTotalFare(
                booking.getTotalFare().add(
                        BigDecimal.valueOf(extra)
                )
        );

        bookingRepository.save(booking);
    }

    @Override
    public String generatePnr() {

        log.debug("Generating unique PNR");

        String pnr;

        do {
            pnr = pnrGenerator.generate();
        } while (
                bookingRepository.findByPnrCode(pnr).isPresent()
        );

        log.info("Generated PNR: {}", pnr);

        return pnr;
    }

    @Override
    public List<BookingResponse> getUpcomingBookings(UUID userId) {

        log.info("Fetching upcoming bookings for userId: {}",
                userId);

        return bookingRepository
                .findByUserIdAndStatus(
                        userId,
                        BookingStatus.CONFIRMED
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public FareResponse calculateFarePreview(
            CreateBookingRequest request
    ) {

        log.info("Calculating fare preview");

        FlightFeignResponse flight =
                flightClient.getFlight(
                        request.getFlightId()
                );

        return calculateFare(request, flight);
    }

    @Override
    public List<BookingResponse> getAllBookings() {

        log.info("Fetching all bookings");

        return bookingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BookingResponse mapToResponse(Booking booking) {

        FlightFeignResponse flight =
                flightClient.getFlight(
                        booking.getFlightId()
                );

        String route = "N/A";

        if (flight != null) {

            route =
                    flight.getOriginAirportCode()
                            + " → " +
                            flight.getDestinationAirportCode();
        }

        List<String> passengers = new ArrayList<>();

        try {

            List<PassengerFeignResponse> passengerList =
                    passengerClient.getPassengersByBookingId(
                            booking.getBookingId()
                    );

            passengers = passengerList.stream()
                    .map(p ->
                            p.getFirstName() + " " +
                                    p.getLastName()
                    )
                    .toList();

        } catch (Exception e) {

            log.error("Failed to fetch passenger details", e);

            passengers = List.of("Unknown");
        }

        return BookingResponse.builder()
                .id(booking.getBookingId())
                .pnr(booking.getPnrCode())
                .passenger(passengers)
                .route(route)
                .amount(booking.getTotalFare().doubleValue())
                .status(booking.getStatus().name())
                .build();
    }

    @Override
    public BookingDetailResponse getBookingDetails(UUID bookingId) {

        log.info("Fetching booking details for bookingId: {}",
                bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new ResourceNotFoundException(
                            "Booking not found"
                    );
                });

        FlightFeignResponse flight = null;

        String route = "N/A";
        String airline = "N/A";

        try {

            flight = flightClient.getFlight(
                    booking.getFlightId()
            );

            if (flight != null) {

                String origin =
                        flight.getOriginAirportCode() != null
                                ? flight.getOriginAirportCode()
                                : "N/A";

                String destination =
                        flight.getDestinationAirportCode() != null
                                ? flight.getDestinationAirportCode()
                                : "N/A";

                route = origin + " → " + destination;

                airline =
                        flight.getAirlineName() != null
                                ? flight.getAirlineName()
                                : "N/A";
            }

        } catch (Exception e) {

            log.error("Failed to fetch flight details for bookingId: {}",
                    bookingId,
                    e);
        }

        List<BookingDetailResponse.PassengerDetail> passengers =
                new ArrayList<>();

        try {

            List<PassengerFeignResponse> list =
                    passengerClient.getPassengersByBookingId(
                            booking.getBookingId()
                    );

            passengers = list.stream()
                    .map(p ->
                            BookingDetailResponse.PassengerDetail
                                    .builder()
                                    .name(
                                            (p.getFirstName() != null
                                                    ? p.getFirstName()
                                                    : "")
                                                    + " " +
                                                    (p.getLastName() != null
                                                            ? p.getLastName()
                                                            : "")
                                    )
                                    .gender(
                                            p.getGender() != null
                                                    ? p.getGender()
                                                    : "N/A"
                                    )
                                    .seatNumber(
                                            p.getSeatNumber() != null
                                                    ? p.getSeatNumber()
                                                    : "N/A"
                                    )
                                    .passportNumber(
                                            p.getPassportNumber() != null
                                                    ? p.getPassportNumber()
                                                    : "N/A"
                                    )
                                    .build()
                    )
                    .toList();

        } catch (Exception e) {

            log.error("Failed to fetch passenger details for bookingId: {}",
                    bookingId,
                    e);
        }

        return BookingDetailResponse.builder()
                .bookingId(booking.getBookingId())
                .flightId(booking.getFlightId())
                .pnr(booking.getPnrCode())
                .status(booking.getStatus().name())
                .route(route)
                .airline(airline)
                .departureTime(
                        (flight != null &&
                                flight.getDepartureTime() != null)
                                ? flight.getDepartureTime().toString()
                                : "N/A"
                )
                .arrivalTime(
                        (flight != null &&
                                flight.getArrivalTime() != null)
                                ? flight.getArrivalTime().toString()
                                : "N/A"
                )
                .passengers(passengers)
                .totalFare(
                        booking.getTotalFare() != null
                                ? booking.getTotalFare().doubleValue()
                                : 0.0
                )
                .baseFare(
                        booking.getBaseFare() != null
                                ? booking.getBaseFare().doubleValue()
                                : 0.0
                )
                .taxes(
                        booking.getTaxes() != null
                                ? booking.getTaxes().doubleValue()
                                : 0.0
                )
                .mealPreference(
                        booking.getMealPreference() != null
                                ? booking.getMealPreference()
                                : "N/A"
                )
                .luggageKg(booking.getLuggageKg())
                .contactEmail(
                        booking.getContactEmail() != null
                                ? booking.getContactEmail()
                                : "N/A"
                )
                .contactPhone(
                        booking.getContactPhone() != null
                                ? booking.getContactPhone()
                                : "N/A"
                )
                .bookedAt(booking.getBookedAt())
                .build();
    }

    private void validateRequest(CreateBookingRequest request) {

        log.debug("Validating booking request");

        if (request.getSeatIds() == null ||
                request.getSeatIds().isEmpty()) {

            throw new BookingException(
                    "Seat selection required"
            );
        }

        if (request.getPassengers() == null ||
                request.getPassengers().isEmpty()) {

            throw new BookingException(
                    "Passengers required"
            );
        }

        if (request.getSeatIds().size() !=
                request.getPassengers().size()) {

            throw new BookingException(
                    "Seat count must match passenger count"
            );
        }
    }
}