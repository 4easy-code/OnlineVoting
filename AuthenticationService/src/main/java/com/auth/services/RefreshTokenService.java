package com.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth.dto.RefreshTokenRequestDto;
import com.auth.response.JwtResponse;
import com.auth.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	@Value("${app.jwt-expiration-milliseconds}")
	private Long jwtExpirationTime;
	
	private final JwtUtil jwtUtil;
	private final TokenStore tokenStore;
	private Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
	
	
	public JwtResponse createAccessToken(RefreshTokenRequestDto token) {
	    String refreshToken = token.getRefreshToken();
	    String username;
	    String newAccessToken;
	    String role;

	    try {
	        username = jwtUtil.extractUsername(refreshToken);

	        // Validate the token itself
	        if (!jwtUtil.validateToken(refreshToken, username) || !tokenStore.isRefreshTokenValid(username, refreshToken)) {
	            logger.info("Invalid or expired refresh token");
	            throw new JwtException("Invalid refresh token");
	        }

	        String tokenType = jwtUtil.extractAllClaim(refreshToken).get("tokenType", String.class);
	        if (!"REFRESH".equals(tokenType)) {
	            logger.warn("Attempt to use non-refresh token for refresh operation");
	            throw new JwtException("Invalid token type for refresh");
	        }

	        role = jwtUtil.extractAllClaim(refreshToken).get("role", String.class).replace("ROLE_", "");
	        newAccessToken = jwtUtil.generateToken(username, role, jwtExpirationTime);
	        
	        tokenStore.storeToken(username, newAccessToken);

	        logger.info("New access token created for user: {}", username);
	        return new JwtResponse(newAccessToken, refreshToken, username, role);

	    } catch (ExpiredJwtException e) {
	        logger.info("Refresh token expired: {}", e.getMessage());
	        throw new JwtException("Refresh token expired");
	    }
	}

}
