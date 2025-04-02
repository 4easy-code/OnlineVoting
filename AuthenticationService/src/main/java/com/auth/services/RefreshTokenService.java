package com.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth.dto.RefreshTokenRequestDto;
import com.auth.exceptions.InvalidRefreshTokenException;
import com.auth.response.JwtResponse;
import com.auth.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	@Value("${app.jwt-expiration-milliseconds}")
	private Long jwtExpirationTime;
	
	private final JwtUtil jwtUtil;
	private final TokenStore tokenStore;
	private Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
	
	
	public JwtResponse createAccessToken(RefreshTokenRequestDto token) throws InvalidRefreshTokenException {
	    String refreshToken = token.getRefreshToken();
	    String username;
	    String newAccessToken;
	    String role;

	    try {
	        username = jwtUtil.extractUsername(refreshToken);

	        // Validate the token itself
	        if (!tokenStore.isRefreshTokenValid(username, refreshToken)) {
	            logger.info("Invalid or expired refresh token");
	            throw new InvalidRefreshTokenException("Invalid or expired Refresh token!");
	        }

	        role = jwtUtil.extractAllClaims(refreshToken).get("role", String.class).replace("ROLE_", "");
	        newAccessToken = jwtUtil.generateToken(username, role, jwtExpirationTime);
	        
	        tokenStore.storeToken(username, newAccessToken);

	        logger.info("New access token created for user: {}", username);
	        return new JwtResponse(newAccessToken, refreshToken, username, role, null);

	    } catch (ExpiredJwtException e) {
	        logger.info("Refresh token expired: {}", e.getMessage());
	        throw new InvalidRefreshTokenException("refresh token expired! Please login again.");
	    }
	}

}
