package com.skybooker.NotificationService.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;



    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        System.out.println(">>> JwtFilter hit: " + request.getServletPath());

        String path = request.getServletPath(); // NOT getRequestURI()
        System.out.println("path : "+path);

        // Bypass entirely for service-to-service endpoints
        if ("/notifications/payment".equals(path)) {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "payment-service", null,
                            List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/stripe/webhook")) {

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "stripe-webhook",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_WEBHOOK"))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            try {
                Claims claims = jwtUtil.validateToken(token);

                Object rolesObj = claims.get("roles");

                List<SimpleGrantedAuthority> authorities;

                if (rolesObj instanceof List<?>) {
                    authorities = ((List<?>) rolesObj).stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();
                } else {
                    String role = claims.get("role").toString();
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
