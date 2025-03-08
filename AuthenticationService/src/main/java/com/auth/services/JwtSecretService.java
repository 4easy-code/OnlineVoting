package com.auth.services;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtSecretService {
	private ConcurrentHashMap<String, String> jwtSecretsMap = new ConcurrentHashMap<>();
	
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
        if (jwtSecretsMap.get(CURRENT_SECRET_KEY) == null) {
            String newSecret = generateSecretKey();
            jwtSecretsMap.put(CURRENT_SECRET_KEY, newSecret);
            logger.info("Generating Initial Secret Key");
        }
    }
    
    public void rotateSecret() {
        String currentSecret = jwtSecretsMap.get(CURRENT_SECRET_KEY);
        String newSecret = generateSecretKey();

        jwtSecretsMap.put(PREVIOUS_SECRET_KEY, currentSecret);        
        jwtSecretsMap.put(CURRENT_SECRET_KEY, newSecret);
        
        logger.info("🔑 JWT Secret Rotated!");
    }
    
    public String getCurrentSecret() {
        return jwtSecretsMap.get(CURRENT_SECRET_KEY);
    }

    public String getPreviousSecret() {
        return jwtSecretsMap.get(PREVIOUS_SECRET_KEY);
    }
}
