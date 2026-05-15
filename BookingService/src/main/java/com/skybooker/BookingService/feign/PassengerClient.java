package com.skybooker.BookingService.feign;

import com.skybooker.BookingService.dto.request.PassengerFeignRequest;
import com.skybooker.BookingService.dto.response.PassengerFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "PASSENGER-SERVICE", url = "http://localhost:8085")
public interface PassengerClient {

    @PostMapping("/passengers/add")
    void addPassenger(@RequestBody PassengerFeignRequest request);

    @GetMapping("/passengers/booking/{bookingId}")
    List<PassengerFeignResponse> getPassengersByBookingId(@PathVariable UUID bookingId);
}
