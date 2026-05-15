package com.skybooker.AirlineAirportService.dto.airport;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AirportRequest {

    @NotBlank
    private String airportName;

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    @NotBlank
    private String iataCode;

    private String icaoCode;
    private String timezone;
    private Double latitude;
    private Double longitude;
}
