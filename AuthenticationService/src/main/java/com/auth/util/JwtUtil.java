package com.auth.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth.filter.JwtFilter;
import com.auth.services.TokenStore;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {
	@Value("${app.jwt-secret}")
	private String jwtSecret;
	@Value("${app.jwt-expiration-milliseconds}")
	private Long jwtExpirationTime;
	
	
	private final TokenStore tokenStore;
	
	private Logger logger = LoggerFactory.getLogger(JwtFilter.class);
	
	public String generateToken(String username, String role) {
		Map<String, String> claims = new HashMap<>();
		claims.put("role", "ROLE_" + role);
		
		String newToken = doGenerateToken(claims, username);
	    tokenStore.storeToken(username, newToken, jwtExpirationTime / 1000); // Store new token, invalidating the old one

	    return newToken;
	}

	public String doGenerateToken(Map<String, String> claims, String subject) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
				.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)), SignatureAlgorithm.HS512)
				.compact();
	}
	
	public Claims extractAllClaim(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
				.setAllowedClockSkewSeconds(2)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
	public String extractUsername(String token) {
		return extractAllClaim(token).getSubject();
	}
	
	public boolean validateToken(String token, String username) {
		final String extractUsername = extractUsername(token);
		return (extractUsername.equals(username) && !isTokenExpired(token));
	}

	public boolean isTokenExpired(String token) {
		return extractAllClaim(token).getExpiration().before(new Date());
	}

}
