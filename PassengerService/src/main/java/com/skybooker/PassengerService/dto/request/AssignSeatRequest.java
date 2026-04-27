package com.skybooker.PassengerService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignSeatRequest {
    @NotNull
    private UUID seatId;

    @NotBlank
    private String seatNumber;
}
