package com.poly.BE_main.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final long jwtExpirationMs;
    private final Key key;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:900000}") long jwtExpirationMs) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(String username, Integer roleId, Integer accountId) {
        return Jwts.builder().setSubject(username)
                .addClaims(Map.of("roleId", roleId, "accountId", accountId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public String getUsernameFromToken(String token) { return getClaims(token).getSubject(); }
    public Integer getRoleIdFromToken(String token) { return getClaims(token).get("roleId", Integer.class); }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token) {
        try { getClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }
}
