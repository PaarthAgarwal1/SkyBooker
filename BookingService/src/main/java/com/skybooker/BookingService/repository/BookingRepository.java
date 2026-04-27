package com.skybooker.BookingService.repository;

import com.skybooker.BookingService.entity.Booking;
import com.skybooker.BookingService.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUserId(UUID userId);

    Optional<Booking> findByPnrCode(String pnrCode);

    List<Booking> findByFlightId(UUID flightId);

    List<Booking> findByStatus(BookingStatus status);

    long countByFlightIdAndStatus(UUID flightId,BookingStatus status);

    List<Booking> findByUserIdAndStatus(UUID userId,BookingStatus status);
}
