package com.skybooker.AirlineAirportService.controller;

import com.skybooker.AirlineAirportService.dto.airport.AirportRequest;
import com.skybooker.AirlineAirportService.dto.airport.AirportResponse;
import com.skybooker.AirlineAirportService.service.AirportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/airport")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService service;

    @PostMapping
    public ResponseEntity<AirportResponse> create(@Valid @RequestBody AirportRequest request) {
        return ResponseEntity.ok(service.createAirport(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getAirportById(id));
    }

    @GetMapping("/code/{iataCode}")
    public ResponseEntity<AirportResponse> getByIata(@PathVariable String iataCode) {
        return ResponseEntity.ok(service.getAirportByIata(iataCode));
    }

    @GetMapping
    public ResponseEntity<List<AirportResponse>> getAll() {
        return ResponseEntity.ok(service.getAllAirports());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<AirportResponse>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(service.getAirportsByCity(city));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirportResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AirportRequest request) {
        return ResponseEntity.ok(service.updateAirport(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        service.deleteAirport(id);
        return ResponseEntity.ok("Airport deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<AirportResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(service.searchAirports(query));
    }

}

