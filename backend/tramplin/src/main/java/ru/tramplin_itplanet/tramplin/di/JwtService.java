package ru.tramplin_itplanet.tramplin.di;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(UserDetails userDetails) {
        log.debug("Generating JWT for user: {}", userDetails.getUsername());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername());
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = resolveSecretBytes(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] resolveSecretBytes(String secretValue) {
        byte[] keyBytes = decodeSecret(secretValue);
        if (keyBytes.length >= 32) {
            return keyBytes;
        }

        log.warn("JWT secret is shorter than 32 bytes after decoding; deriving a 32-byte key via SHA-256");
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(secretValue.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private byte[] decodeSecret(String secretValue) {
        try {
            return Decoders.BASE64.decode(secretValue);
        } catch (IllegalArgumentException ignored) {
            // Fall through and try URL-safe Base64 or plain text.
        }

        try {
            return Decoders.BASE64URL.decode(secretValue);
        } catch (IllegalArgumentException ignored) {
            return secretValue.getBytes(StandardCharsets.UTF_8);
        }
    }
}
