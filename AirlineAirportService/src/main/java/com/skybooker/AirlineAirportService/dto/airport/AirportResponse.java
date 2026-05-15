package com.skybooker.AirlineAirportService.dto.airport;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class AirportResponse {

    private UUID airportId;
    private String airportName;
    private String city;
    private String country;
    private String iataCode;
    private String icaoCode;
    private String timezone;
}
