package com.skybooker.BookingService.dto.response;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class FlightFeignResponse {
    private UUID flightId;
    private Integer availableSeats;
    private BigDecimal basePrice;
}
