package com.skybooker.AirlineAirportService.dto.airline;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class AirlineResponse {
    private UUID airlineId;
    private String airlineName;
    private String iataCode;
    private String country;
    private String logoUrl;
    private boolean isActive;
}
