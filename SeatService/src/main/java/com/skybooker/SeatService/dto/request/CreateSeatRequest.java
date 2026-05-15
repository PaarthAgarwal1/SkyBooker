package com.skybooker.SeatService.dto.request;

import com.skybooker.SeatService.entity.SeatClass;
import lombok.Data;

@Data
public class CreateSeatRequest {
    private String seatNumber;
    private SeatClass seatClass;
    private int rowNumber;
    private int columnNumber;
    private boolean isWindow;
    private boolean isAisle;
    private boolean hasExtraLegroom;
    private double priceMultiplier;
}
