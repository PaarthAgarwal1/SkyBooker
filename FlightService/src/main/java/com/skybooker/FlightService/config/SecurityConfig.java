package com.skybooker.FlightService.config;

import com.skybooker.FlightService.security.JwtFilter;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // Public APIs
                        .requestMatchers("/flights/search", "/flights/round-trip","/swagger-ui/**","/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.POST,"/flights").hasRole("AIRLINE_STAFF")

                        // Protected APIs
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

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