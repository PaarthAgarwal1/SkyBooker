// ServiceTokenInterceptor.java (Payment Service)
package com.skybooker.PaymentService.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceTokenInterceptor implements RequestInterceptor {

    @Value("${service.auth.token}")
    private String serviceToken;

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-SERVICE-TOKEN", serviceToken);
    }
}