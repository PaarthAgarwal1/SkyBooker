package com.skybooker.AuthService.dto.response;
import lombok.Data;

import java.util.UUID;

@Data
public class AirlineResponse {

    private UUID airlineId;
    private String airlineName;
    private String iataCode;
    private String country;
    private String logoUrl;
    private boolean isActive;
}
