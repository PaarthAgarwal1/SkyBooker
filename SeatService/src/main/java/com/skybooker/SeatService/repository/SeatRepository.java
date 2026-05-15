package com.skybooker.SeatService.repository;

import com.skybooker.SeatService.entity.Seat;
import com.skybooker.SeatService.entity.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByFlightId(UUID flightId);

    List<Seat> findByFlightIdAndSeatClass(UUID flightId, SeatClass seatClass);

    Seat findByFlightIdAndSeatNumber(UUID flightId,String seatNumber);

    @Query("SELECT s FROM Seat s WHERE s.flightId = :flightId AND s.status ='AVAILABLE' ")
    List<Seat> findAvailableSeats(UUID flightId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.flightId = :flightId AND s.status= 'AVAILABLE' ")
    long countAvailableSeas(UUID flightId);

    @Query("""
        SELECT COUNT(s) FROM Seat s 
        WHERE s.flightId = :flightId 
        AND s.seatClass = :seatClass 
        AND s.status = 'AVAILABLE'
    """)
    long countAvailableByClass(UUID flightId, SeatClass seatClass);
}
