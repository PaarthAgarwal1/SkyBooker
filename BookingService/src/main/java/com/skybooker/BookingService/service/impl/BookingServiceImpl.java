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
    private final PaymentClient paymentClient;
    private final PnrGenerator pnrGenerator;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        validateRequest(request);

        // 1. Idempotency Check
        if (request.getIdempotencyKey() != null) {
            Optional<Booking> existing = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return mapToResponse(existing.get());
            }
        }

        // 2. Initial Checks
        FlightFeignResponse flight = flightClient.getFlight(request.getFlightId());
        if (flight == null) {
            throw new ResourceNotFoundException("Flight not found");
        }

        if (flight.getAvailableSeats() < request.getSeatIds().size()) {
            throw new BookingException("Not enough seats available");
        }

        // 3. Hold Seats (Sync - critical for immediate feedback)
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
            throw new BookingException("Seat hold failed: " + e.getMessage());
        }

        FareResponse fare = calculateFare(request, flight);

        // 4. Create Booking in PAYMENT_PENDING status
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .flightId(request.getFlightId())
                .seatIds(request.getSeatIds())
                .pnrCode(generatePnr())
                .tripType(request.getTripType())
                .status(BookingStatus.PAYMENT_PENDING) // Set to PAYMENT_PENDING
                .idempotencyKey(request.getIdempotencyKey())
                .expiryTime(LocalDateTime.now().plusMinutes(5)) // 15 mins TTL
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

        // 5. Register Passengers
        try {
            for (AddPassengerRequest p : request.getPassengers()) {
                if (!request.getSeatIds().contains(p.getSeatId())) {
                    throw new BookingException("Invalid seat mapping");
                }
                PassengerFeignRequest passenger=PassengerFeignRequest.builder()
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
                        .passportExpiry(p.getPassportExpiry()).build();
                passengerClient.addPassenger(passenger);
            }
        } catch (Exception e) {
            heldSeats.forEach(seatClient::releaseSeat);
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            throw new BookingException("Passenger creation failed: " + e.getMessage());
        }

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(UUID bookingId, ConfirmBookingRequest request) {

        System.out.println("booking confirm request from front end "+request);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        System.out.println("booking status for confer booking "+booking.getStatus());
        // ✅ Idempotency
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return mapToResponse(booking);
        }

        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new BookingException("Invalid booking state"+booking.getStatus());
        }

        // ✅ VERIFY PAYMENT
        PaymentStatusResponse paymentStatus =
                paymentClient.getPaymentStatus(request.getPaymentId());

        System.out.println("payment status for confirm booking "+ paymentStatus);

        if (paymentStatus == null || paymentStatus.getStatus() != PaymentStatus.PAID) {

            booking.getSeatIds().forEach(seatClient::releaseSeat);
            booking.setStatus(BookingStatus.CANCELLED);

            return mapToResponse(booking);
        }

        // ✅ CONFIRM SEATS
        booking.getSeatIds().forEach(seatClient::confirmSeat);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentId(request.getPaymentId());

        // ✅ UPDATE FLIGHT
        flightClient.decrementSeats(
                booking.getFlightId(),
                booking.getSeatIds().size()
        );

        FlightFeignResponse flight = flightClient.getFlight(booking.getFlightId());

        // ✅ GET PASSENGERS
        List<PassengerFeignResponse> passengers =
                passengerClient.getPassengersByBookingId(booking.getBookingId());

        // ✅ Combine Passenger Name + Seat
        List<String> passengerDetails = passengers.stream()
                .map(p ->
                        p.getFirstName() + " " +
                                p.getLastName() +
                                " - Seat " +
                                p.getSeatNumber()
                )
                .toList();

        // ✅ NOTIFICATION
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
    public BookingStatus getBookingStatus(UUID id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return booking.getStatus();
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

        RefundRequest refundRequest=RefundRequest.builder()
                .paymentId(booking.getPaymentId())
                .refundAmount(booking.getTotalFare().doubleValue())
                .build();

        paymentClient.refund(refundRequest);

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
    public FareResponse calculateFare(CreateBookingRequest request,
                                      FlightFeignResponse flight) {

        int passengerCount = request.getPassengers().size();

        // ✅ Base fare from flight
        double baseFarePerPassenger = flight.getBasePrice().doubleValue();

        // ✅ Total base fare
        double baseFare = baseFarePerPassenger * passengerCount;

        // =========================================
        // ✅ SEAT CHARGES
        // =========================================

        double seatCharges = 0;

        for (UUID seatId : request.getSeatIds()) {

            SeatFeignResponse seat = seatClient.getSeat(seatId);

            if (seat == null) {
                throw new BookingException("Seat not found: " + seatId);
            }

            // Example:
            // 1.0 = normal
            // 1.3 = premium
            // 2.0 = business

            double extraMultiplier = seat.getPriceMultiplier() - 1;

            if (extraMultiplier > 0) {
                seatCharges += baseFarePerPassenger * extraMultiplier;
            }
        }

        // =========================================
        // ✅ MEAL CHARGES
        // =========================================

        double mealCharges = 0;

        if (request.getMealPreference() != null) {

            switch (request.getMealPreference().toUpperCase()) {

                case "VEG":
                    mealCharges = 250 * passengerCount;
                    break;

                case "NONVEG":
                    mealCharges = 400 * passengerCount;
                    break;

                case "VEGAN":
                    mealCharges = 350 * passengerCount;
                    break;

                case "JAIN":
                    mealCharges = 300 * passengerCount;
                    break;

                default:
                    mealCharges = 0;
            }
        }

        // =========================================
        // ✅ BAGGAGE CHARGES
        // =========================================

        double baggageCharges = 0;

        // Assume 15kg free
        int freeLimit = 15;

        if (request.getLuggageKg() > freeLimit) {

            int extraKg = request.getLuggageKg() - freeLimit;

            baggageCharges = extraKg * 80;
        }

        // =========================================
        // ✅ AIRLINE FEES
        // =========================================

        double fuelSurcharge = 1200;

        double convenienceFee = 250;

        // =========================================
        // ✅ SUBTOTAL
        // =========================================

        double subtotal =
                baseFare +
                        seatCharges +
                        mealCharges +
                        baggageCharges +
                        fuelSurcharge +
                        convenienceFee;

        // =========================================
        // ✅ TAXES (5% GST)
        // =========================================

        double taxes = subtotal * 0.05;

        // =========================================
        // ✅ FINAL TOTAL
        // =========================================

        double totalFare = subtotal + taxes;

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

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    private BookingResponse mapToResponse(Booking booking) {

        // 🔹 Get Flight Info
        FlightFeignResponse flight = flightClient.getFlight(booking.getFlightId());

        String route = "N/A";
        if (flight != null) {
            route = flight.getOriginAirportCode() + " → " + flight.getDestinationAirportCode();
        }

        // 🔹 Get Passenger Info
        List<String> passengers = new ArrayList<>();
        try {
            List<PassengerFeignResponse> passengerList =
                    passengerClient.getPassengersByBookingId(booking.getBookingId());

            passengers = passengerList.stream()
                    .map(p -> p.getFirstName() + " " + p.getLastName())
                    .toList();

        } catch (Exception e) {
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

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // 🔹 Flight Info
        FlightFeignResponse flight = null;
        String route = "N/A";
        String airline = "N/A";

        try {
            flight = flightClient.getFlight(booking.getFlightId());

            if (flight != null) {
                String origin = flight.getOriginAirportCode() != null ? flight.getOriginAirportCode() : "N/A";
                String destination = flight.getDestinationAirportCode() != null ? flight.getDestinationAirportCode() : "N/A";

                route = origin + " → " + destination;
                airline = flight.getAirlineName() != null ? flight.getAirlineName() : "N/A";
            }

        } catch (Exception e) {
            e.printStackTrace(); // Replace with logger in production
        }

        // 🔹 Passenger Info
        List<BookingDetailResponse.PassengerDetail> passengers = new ArrayList<>();

        try {
            List<PassengerFeignResponse> list =
                    passengerClient.getPassengersByBookingId(booking.getBookingId());

            passengers = list.stream().map(p ->
                    BookingDetailResponse.PassengerDetail.builder()
                            .name(
                                    (p.getFirstName() != null ? p.getFirstName() : "") + " " +
                                            (p.getLastName() != null ? p.getLastName() : "")
                            )
                            .gender(p.getGender() != null ? p.getGender() : "N/A")
                            .seatNumber(p.getSeatNumber() != null ? p.getSeatNumber() : "N/A")
                            .passportNumber(p.getPassportNumber() != null ? p.getPassportNumber() : "N/A")
                            .build()
            ).toList();

        } catch (Exception e) {
            e.printStackTrace(); // Replace with logger
        }

        // 🔹 Build Response
        return BookingDetailResponse.builder()
                .bookingId(booking.getBookingId())
                .flightId(booking.getFlightId()) // ✅ FIXED
                .pnr(booking.getPnrCode())
                .status(booking.getStatus().name())

                .route(route)
                .airline(airline)

                .departureTime(
                        (flight != null && flight.getDepartureTime() != null)
                                ? flight.getDepartureTime().toString()
                                : "N/A"
                )
                .arrivalTime(
                        (flight != null && flight.getArrivalTime() != null)
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

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new BookingException("Seat selection required");
        }

        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            throw new BookingException("Passengers required");
        }

        if (request.getSeatIds().size() != request.getPassengers().size()) {
            throw new BookingException("Seat count must match passenger count");
        }
    }
}