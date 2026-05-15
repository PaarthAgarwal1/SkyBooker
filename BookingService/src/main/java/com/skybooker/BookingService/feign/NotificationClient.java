package com.skybooker.BookingService.feign;

import com.skybooker.BookingService.dto.request.BookingEmailRequest;
import com.skybooker.BookingService.dto.request.CancellationEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {

    @PostMapping("/notifications/booking-confirmation")
    void sendBooking(@RequestBody BookingEmailRequest request);

    @PostMapping("/notifications/cancellation")
    void sendCancellation(@RequestBody CancellationEmailRequest request);
}
