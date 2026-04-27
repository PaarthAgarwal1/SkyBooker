package com.skybooker.AirlineAirportService.dto.airline;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AirlineRequest {

    @NotBlank
    private String airlineName;

    @NotBlank
    private String iataCode;

    private String country;
    private String logoUrl;
}
