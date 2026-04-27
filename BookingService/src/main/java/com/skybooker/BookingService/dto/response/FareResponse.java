package com.skybooker.BookingService.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FareResponse {
    private double baseFare;
    private double taxes;
    private double totalFare;
}
