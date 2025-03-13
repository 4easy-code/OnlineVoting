package com.auth.services;

import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtSecretService {
	private final StringRedisTemplate redisTemplate;
	
	private static final String CURRENT_SECRET_KEY = "jwtSecret:current";
    private static final String PREVIOUS_SECRET_KEY = "jwtSecret:previous";
    
    private Logger logger = LoggerFactory.getLogger(JwtSecretService.class);
    
    
    private String generateSecretKey() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
    
    @PostConstruct
    public void initializeSecret() {
    	if (!Boolean.TRUE.equals(redisTemplate.hasKey(CURRENT_SECRET_KEY))) {
            String newSecret = generateSecretKey();
            redisTemplate.opsForValue().set(CURRENT_SECRET_KEY, newSecret);
            logger.info("Generating Initial Secret Key");
        }
    }
    
    public void rotateSecret() {
        String currentSecret = redisTemplate.opsForValue().get(CURRENT_SECRET_KEY);
        String newSecret = generateSecretKey();

        if (currentSecret != null) {
            redisTemplate.opsForValue().set(PREVIOUS_SECRET_KEY, currentSecret);
        }
        redisTemplate.opsForValue().set(CURRENT_SECRET_KEY, newSecret);
        
        logger.info("🔑 JWT Secret Rotated!");
    }
    
    public String getCurrentSecret() {
    	return redisTemplate.opsForValue().get(CURRENT_SECRET_KEY);
    }

    public String getPreviousSecret() {
    	return redisTemplate.opsForValue().get(PREVIOUS_SECRET_KEY);
    }
}
