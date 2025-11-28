package com.vote.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vote.services.JwtSecretService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtSecretService jwtSecretService;
    private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
 
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
