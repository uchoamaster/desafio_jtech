package br.com.jtech.tasklist.application.core.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final String secret;
    private final long expirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-seconds}") long expirationSeconds,
            @Value("${app.security.jwt-refresh-expiration-seconds}") long refreshExpirationSeconds
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String generateToken(String subject) {
        return generateToken(subject, ACCESS_TOKEN_TYPE, expirationSeconds);
    }

    public String generateRefreshToken(String subject) {
        return generateToken(subject, REFRESH_TOKEN_TYPE, refreshExpirationSeconds);
    }

    public boolean isAccessTokenValid(String token, String subject) {
        return isTokenValid(token, subject, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshTokenValid(String token, String subject) {
        return isTokenValid(token, subject, REFRESH_TOKEN_TYPE);
    }

    private String generateToken(String subject, String tokenType, long expiresInSeconds) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresInSeconds)))
                .signWith(signingKey())
                .compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String subject, String expectedType) {
        var claims = parseClaims(token);
        var tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        return subject.equalsIgnoreCase(claims.getSubject())
                && expectedType.equals(tokenType)
                && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}