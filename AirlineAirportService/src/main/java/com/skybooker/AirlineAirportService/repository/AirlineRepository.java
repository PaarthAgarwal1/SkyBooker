package com.skybooker.AirlineAirportService.repository;

import com.skybooker.AirlineAirportService.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, UUID> {

    Optional<Airline> findByIataCode(String iataCode);

    List<Airline> findByIsActive(boolean isActive);

    boolean existsByIataCode(String iataCode);
}
