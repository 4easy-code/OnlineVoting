package com.auth.services;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

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
	public final ConcurrentHashMap<String, Deque<String>> activeSessions = new ConcurrentHashMap<>();
	
    public static final String TOKEN_PREFIX_ACCESS = "access_token:";
    private static final String TOKEN_PREFIX_REFRESH = "refresh_token:";
    private static final int MAX_SESSIONS = 3;
    
    private Logger logger = LoggerFactory.getLogger(TokenService.class);
 
    public synchronized void storeToken(String username, String token) {
        String key = TOKEN_PREFIX_ACCESS + username;
        activeSessions.putIfAbsent(key, new LinkedList<>());
        Deque<String> sessions = activeSessions.get(key);

        if (sessions.size() >= MAX_SESSIONS) {
            String removedToken = sessions.removeFirst();
            logger.info("Removed oldest session: {} ", removedToken);
        }

        sessions.addLast(token);
        logger.info("Stored token under key: {}", key);
    }

    
    public void storeRefreshToken(String username, String token) {
    	String key = TOKEN_PREFIX_REFRESH + username;
        redisTemplate.delete(key);
        redisTemplate.opsForValue().set(key, token);
    }
    
    public boolean isTokenValid(String username, String token) {
        String key = TOKEN_PREFIX_ACCESS + username; // Must match the one in storeToken()
        return activeSessions.containsKey(key) && activeSessions.get(key).contains(token);
    }

    
    public boolean isRefreshTokenValid(String username, String token) {
    	String key = TOKEN_PREFIX_REFRESH + username;
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

    
    public void removeToken(String username, String token) {
        activeSessions.getOrDefault(TOKEN_PREFIX_ACCESS + username, new LinkedList<>()).remove(token);
    }
    
    public void removeAllTokens(String username) {
        activeSessions.remove(TOKEN_PREFIX_ACCESS + username);
    }
}
