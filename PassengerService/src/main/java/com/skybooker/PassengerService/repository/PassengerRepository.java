package com.skybooker.PassengerService.repository;

import com.skybooker.PassengerService.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger,UUID> {

    List<Passenger> findByBookingId(UUID bookingId);

    List<Passenger> findByFlightId(UUID flightId);

    long countByBookingId(UUID bookingId);

    Passenger findByPassportNumber(String passportNumber);

    Passenger findByTicketNumber(String ticketNumber);

    Passenger findBySeatId(UUID seatId);

    void deleteByBookingId(UUID bookingId);

    boolean existsByTicketNumber(String ticket);
}
