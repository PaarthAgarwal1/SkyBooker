package com.skybooker.PaymentService.config;

import com.skybooker.PaymentService.security.JwtFilter;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${service.auth.token}")
    private String serviceToken;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                                // ✅ public
                                .requestMatchers(
                                        "/stripe/webhook",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()

                                // ✅ admin only
//                        .requestMatchers(HttpMethod.POST, "/")
//                        .hasRole("ADMIN")

                                // ✅ others need auth
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
//            requestTemplate.header("X-SERVICE-TOKEN", serviceToken);
//        };
//    }
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            var auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getDetails() != null) {
                String token = auth.getDetails().toString();
                requestTemplate.header("Authorization", token);
            }
        };
    }
}
