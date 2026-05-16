package com.skybooker.AuthService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret:my-super-secret-key-my-super-secret-key-12345}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (jwtSecret.length() < 32) {
            throw new RuntimeException("JWT secret must be at least 32 characters long");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ✅ UPDATED METHOD (with airlineId)
    public String generateToken(String email, String role, UUID airlineId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("airlineId", airlineId != null ? airlineId.toString() : null) // ⭐ important
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    // ✅ Extract Email
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ Extract Role
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ✅ NEW: Extract AirlineId
    public UUID extractAirlineId(String token) {
        Object value = getClaims(token).get("airlineId");

        if (value == null) {
            return null; // For ADMIN / PASSENGER
        }

        return UUID.fromString(value.toString());
    }

    // ✅ Validate Token
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Get Claims
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ✅ Extract JWT from Header
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
