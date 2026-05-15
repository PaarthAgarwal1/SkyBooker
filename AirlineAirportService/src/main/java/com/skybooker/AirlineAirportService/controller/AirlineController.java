package com.skybooker.AirlineAirportService.controller;

import com.skybooker.AirlineAirportService.dto.airline.AirlineRequest;
import com.skybooker.AirlineAirportService.dto.airline.AirlineResponse;
import com.skybooker.AirlineAirportService.service.AirlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("airline")
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineService service;

    @PostMapping
    public ResponseEntity<AirlineResponse> create(@Valid @RequestBody AirlineRequest request){
        return ResponseEntity.ok(service.createAirline(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirlineResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getAirlineById(id));
    }

    @GetMapping("/code/{iataCode}")
    public ResponseEntity<AirlineResponse> getByIata(@PathVariable String iataCode){
        return ResponseEntity.ok(service.getAirlineByIata(iataCode));
    }

    @GetMapping
    public ResponseEntity<List<AirlineResponse>> getAll(){
        return ResponseEntity.ok(service.getAllAirlines());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirlineResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody AirlineRequest request){
        return ResponseEntity.ok(service.updateAirline(id,request));
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<String> activate(@PathVariable UUID id){
        service.activateAirline(id);
        return ResponseEntity.ok("Airline activated successfully");
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<String> deactivate(@PathVariable UUID id) {
        service.deactivateAirline(id);
        return ResponseEntity.ok("Airline deactivated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        service.deleteAirline(id);
        return ResponseEntity.ok("Airline deleted successfully");
    }
}
