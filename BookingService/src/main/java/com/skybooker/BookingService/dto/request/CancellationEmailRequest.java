package com.skybooker.BookingService.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancellationEmailRequest {
    private String email;
    private String pnr;
}