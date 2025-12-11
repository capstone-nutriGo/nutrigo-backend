package com.nutrigo.nutrigo_backend.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMillis;

    private SecretKey key;

    @PostConstruct
    public void initializeKey() {
        byte[] keyBytes;
        try {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId) {
        return buildToken(userId, expirationMillis);
    }

    public String generateRefreshToken(Long userId) {
        return buildToken(userId, expirationMillis);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    private String buildToken(Long userId, long validityMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}