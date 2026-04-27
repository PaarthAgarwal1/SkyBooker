package com.skybooker.SeatService.dto.response;

import com.skybooker.SeatService.entity.SeatClass;
import com.skybooker.SeatService.entity.SeatStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SeatResponse {
    private UUID seatId;
    private String seatNumber;
    private SeatClass seatClass;
    private SeatStatus status;
    private int rowNumber;
    private int columnNumber;
    private boolean isWindow;
    private boolean isAisle;
    private boolean hasExtraLegroom;
    private double priceMultiplier;
    private LocalDateTime holdExpiryTime;
}
