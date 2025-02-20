package com.auth.services;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenStore {
	private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_PREFIX = "user_token:";
    
    private Logger logger = LoggerFactory.getLogger(TokenService.class);
    
    
    // Store a user's token in Redis (invalidate previous session). 
    public void storeToken(String username, String token, long expirationSeconds) {
    	logger.info("trying to set token in redis cache");
    	logger.info("Storing token for {}: {}", username, token);
        String key = TOKEN_PREFIX + username;

        // Remove any old session (only one session per user)
        redisTemplate.delete(key);

        // Store new token with expiration
        redisTemplate.opsForValue().set(key, token, expirationSeconds, TimeUnit.SECONDS);
        
        logger.info("key store: {}", redisTemplate.opsForValue().get(key));
    }
    
    public boolean isValidToken(String username, String token) {
        String key = TOKEN_PREFIX + username;
        String storedToken = redisTemplate.opsForValue().get(key);

        logger.info("Checking token for {}: Stored={}, Incoming={}", username, storedToken, token);

        if (storedToken == null) {
            logger.error("Stored token is NULL. Maybe it expired?");
            return false;
        }

        boolean isValid = token.equals(storedToken);
        
        if (!isValid) {
            logger.error("Token MISMATCH! Possible causes: expired session, different devices, or invalidation.");
        }
        
        return isValid;
    }

    
    // Invalidate a user's session (on logout).
    public void invalidateToken(String username) {
        redisTemplate.delete(TOKEN_PREFIX + username);
    }
}
