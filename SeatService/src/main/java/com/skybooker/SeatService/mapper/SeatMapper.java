package com.skybooker.SeatService.mapper;

import com.skybooker.SeatService.dto.response.SeatResponse;
import com.skybooker.SeatService.entity.Seat;

public class SeatMapper {

    public static SeatResponse toResponse(Seat seat) {
        return SeatResponse.builder()
                .seatId(seat.getSeatId())
                .seatNumber(seat.getSeatNumber())
                .seatClass(seat.getSeatClass())
                .status(seat.getStatus())
                .rowNumber(seat.getRowNumber())
                .columnNumber(seat.getColumnNumber())
                .isWindow(seat.isWindow())
                .isAisle(seat.isAisle())
                .hasExtraLegroom(seat.isHasExtraLegroom())
                .priceMultiplier(seat.getPriceMultiplier())
                .holdExpiryTime(seat.getHoldExpiryTime())
                .build();
    }
}
