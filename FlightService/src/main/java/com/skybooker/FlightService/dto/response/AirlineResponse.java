package com.skybooker.FlightService.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // 🔥 IMPORTANT
public class AirlineResponse {

    private UUID airlineId;
    private String airlineName;
}