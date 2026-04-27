package com.skybooker.FlightService.repository;

import com.skybooker.FlightService.entity.Flight;
import com.skybooker.FlightService.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlightRepository extends JpaRepository<Flight, UUID> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findByAirlineId(UUID airlineId);

    List<Flight> findByStatus(FlightStatus status);

    long countByAirlineId(UUID airlineId);

    @Query("""
    SELECT f FROM Flight f
    WHERE f.originAirportCode = :origin
    AND f.destinationAirportCode = :destination
    AND f.departureTime BETWEEN :start AND :end
    AND f.availableSeats > 0
""")
    List<Flight> findByOriginAndDestAndDate(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT f FROM Flight f
        WHERE f.availableSeats > 0
    """)
    List<Flight> findAvailableFlights();
}
