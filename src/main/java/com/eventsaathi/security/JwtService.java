package com.eventsaathi.security;

import com.eventsaathi.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMillis) {
        byte[] keyBytes = secret.startsWith("base64:")
                ? Decoders.BASE64.decode(secret.substring("base64:".length()))
                : secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        this.expirationMillis = expirationMillis;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(signingKey)
                .compact();
    }

    public String extractSubject(String token) {
        return claims(token).getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload();
    }
}
