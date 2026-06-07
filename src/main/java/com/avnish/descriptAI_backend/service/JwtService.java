package com.avnish.descriptAI_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // ✅ minimum 32 characters (256 bits) required
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expirationMs ;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Generate Token ───────────────────────────────────
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())       // ✅ 0.12.x
                .issuedAt(new Date())                     // ✅ 0.12.x
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // ✅ 0.12.x
                .signWith(getSigningKey())
                .compact();
    }

    // ── Extract Username ─────────────────────────────────
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ── Validate Token ───────────────────────────────────
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ── Check Expiry ─────────────────────────────────────
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date()); // ✅ method reference
    }

    // ── Extract Any Claim ────────────────────────────────
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}