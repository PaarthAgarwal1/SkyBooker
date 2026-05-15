package com.skybooker.PaymentService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RouteRevenue {

    private String route;

    private double revenue;
}