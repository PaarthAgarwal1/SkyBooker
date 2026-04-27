package com.skybooker.AirlineAirportService.repository;

import com.skybooker.AirlineAirportService.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AirportRepository extends JpaRepository<Airport, UUID> {
    Optional<Airport> findByIataCode(String iataCode);

    boolean existsByIataCode(String iataCode);

    List<Airport> findByCity(String city);

    List<Airport> findByCountry(String country);

    @Query("""
        SELECT a FROM Airport a
        WHERE LOWER(a.city) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(a.airportName) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(a.iataCode) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Airport> searchAirports(@Param("query") String query);

    @Query("""
        SELECT a FROM Airport a
        WHERE LOWER(a.city) LIKE LOWER(CONCAT(:query, '%'))
        OR LOWER(a.airportName) LIKE LOWER(CONCAT(:query, '%'))
        ORDER BY a.city ASC
    """)
    List<Airport> autocomplete(@Param("query") String query, Pageable pageable);



}
