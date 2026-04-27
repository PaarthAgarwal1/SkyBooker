package com.skybooker.NotificationService.config;

import com.skybooker.NotificationService.security.JwtFilter;
import com.skybooker.NotificationService.security.ServiceAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ServiceAuthFilter filter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("request come in security filter chain : "+http);
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/notifications/payment",
                                "/stripe/webhook",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // FIRST: Service filter
//                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)

                // SECOND: JWT filter AFTER service filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    public FilterRegistrationBean<ServiceAuthFilter> disableServiceAuthFilterAutoReg(
            ServiceAuthFilter filter) {
        FilterRegistrationBean<ServiceAuthFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false); // stops Spring Boot from registering it as a raw servlet filter
        return reg;
    }
}

