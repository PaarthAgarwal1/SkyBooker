package com.skybooker.PaymentService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponse {

    private double totalRevenue;

    private double growthPercentage;

    private List<RouteRevenue> routeRevenue;

    private CabinDistribution cabinDistribution;

    private List<RevenueTrend> revenueTrends;
}