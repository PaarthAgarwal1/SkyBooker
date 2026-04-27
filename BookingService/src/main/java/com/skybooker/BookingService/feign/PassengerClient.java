package com.skybooker.BookingService.feign;

import com.skybooker.BookingService.dto.request.PassengerFeignRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PASSENGER-SERVICE", url = "http://localhost:8085")
public interface PassengerClient {

    @PostMapping("/passengers/add")
    void addPassenger(@RequestBody PassengerFeignRequest request);
}
