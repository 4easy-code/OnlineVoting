package com.auth.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.auth.services.JwtSecretService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtSecretService jwtSecretService;
    private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public String generateToken(String username, String role, long expirationTime) {
        logger.info("Generating access token for {}", username);

        Map<String, String> claims = new HashMap<>();
        claims.put("role", "ROLE_" + role);
        claims.put("tokenType", "ACCESS");

        return createToken(claims, username, expirationTime);
    }

    public String generateRefreshToken(String username, String role, long expirationTime) {
        logger.info("Generating refresh token for {}", username);

        Map<String, String> claims = new HashMap<>();
        claims.put("role", "ROLE_" + role);
        claims.put("tokenType", "REFRESH");

        return createToken(claims, username, expirationTime);
    }

    private String createToken(Map<String, String> claims, String subject, long expirationTime) {
        String secret = jwtSecretService.getCurrentSecret();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        try {
            return parseToken(token, jwtSecretService.getCurrentSecret());
        } catch (JwtException e1) {
            logger.warn("Token verification failed with current secret. Trying previous secret...");
            try {
                return parseToken(token, jwtSecretService.getPreviousSecret());
            } catch (JwtException e2) {
                logger.error("Token verification failed with both current and previous secrets: {}", e2.getMessage());
                return null;
            }
        }
    }

    private Claims parseToken(String token, String secret) {
        if (secret == null) {
            throw new JwtException("JWT secret is not available!");
        }
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .setAllowedClockSkewSeconds(2)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return (claims != null) ? claims.getSubject() : null;
    }

    public boolean validateToken(String token, String username) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return false;
        }
        return claims.getSubject().equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null && claims.getExpiration().before(new Date());
    }
}

