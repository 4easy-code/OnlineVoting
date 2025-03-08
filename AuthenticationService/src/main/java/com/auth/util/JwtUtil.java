package com.auth.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
	
	private Logger logger = LoggerFactory.getLogger(JwtUtil.class);
	
	public String generateToken(String username, String role, long expirationTime) {
		logger.info("generating token ...");
		
		Map<String, String> claims = new HashMap<>();
		claims.put("role", "ROLE_" + role);
		claims.put("tokenType", "ACCESS");
		
		String newToken = doGenerateToken(claims, username, expirationTime);

	    return newToken;
	}

	public String doGenerateToken(Map<String, String> claims, String subject, long expirationTime) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)), SignatureAlgorithm.HS512)
				.compact();
	}
	
	public String generateRefreshToken(String username, String role, long expirationTime) {
		Map<String, String> claims = new HashMap<>();
		claims.put("role", "ROLE_" + role);
		claims.put("tokenType", "REFRESH");
		
		String newToken = Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)), SignatureAlgorithm.HS512)
				.compact();
		
		return newToken;
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
