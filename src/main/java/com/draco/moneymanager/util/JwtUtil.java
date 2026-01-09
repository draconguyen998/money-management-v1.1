package com.draco.moneymanager.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}") // Base64 string
    private String jwtSecretBase64;

    @Value("${jwt.expiration-ms:86400000}") // default 24h
    private long expirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        // Decode Base64 secret -> key bytes
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretBase64);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token with subject=email
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract subject (email) from token
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Validate token signature + expiration
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            getClaims(token); // will throw if invalid/expired
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
