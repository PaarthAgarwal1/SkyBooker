package com.skybooker.PassengerService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;

@Component
public class JwtUtil {
    @Value("${jwt.secret:my-super-secret-key-my-super-secret-key-12345}")
    private String SECRET;

    private Key getKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public Claims validateToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
