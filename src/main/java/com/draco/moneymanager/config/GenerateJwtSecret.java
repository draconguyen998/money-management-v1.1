package com.draco.moneymanager.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Base64;

public class GenerateJwtSecret {
    public static void main(String[] args) {
        // Generate a strong random key for HS256
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();

        // Print as Base64 so it's easy to store in env var / properties
        String base64Secret = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("JWT_SECRET (Base64) =");
        System.out.println(base64Secret);
    }
}
