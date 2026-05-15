package com.skybooker.PaymentService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinDistribution {

    private double economy;

    private double business;

    private double firstClass;
}