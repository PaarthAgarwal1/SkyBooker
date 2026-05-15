package com.skybooker.PaymentService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueTrend {

    private String date;

    private double revenue;
}