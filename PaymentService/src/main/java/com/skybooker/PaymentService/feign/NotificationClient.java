package com.skybooker.PaymentService.feign;


import com.skybooker.PaymentService.dto.request.PaymentEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {


    @PostMapping("/notifications/payment")
    void sendPayment(@RequestBody PaymentEmailRequest request);
}