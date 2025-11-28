package com.vote.services;



import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.vote.feign.JwtSecretClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtSecretService {

    private final JwtSecretClient jwtSecretClient;
    private final Cache<String, String> jwtSecretCache;

    public String getCurrentSecret() {
        @Nullable
		String cachedSecret = jwtSecretCache.getIfPresent("current");
        if(cachedSecret != null) {
        	return cachedSecret;
        }
        
        String currentSecret = jwtSecretClient.getCurrentSecret();
        jwtSecretCache.put("current", currentSecret);
    	
    	return currentSecret;
    }

    public String getPreviousSecret() {
        @Nullable
		String cachedSecret = jwtSecretCache.getIfPresent("previous");
        if(cachedSecret != null) {
        	return cachedSecret;
        }
        String previousSecret = jwtSecretClient.getPreviousSecret();
        jwtSecretCache.put("previous", previousSecret);
        
        return previousSecret;
    }


    @Scheduled(fixedRate = 10 * 60 * 1000) // 10 minutes
    public void refreshSecrets() {
        jwtSecretCache.put("current", jwtSecretClient.getCurrentSecret());
        jwtSecretCache.put("previous", jwtSecretClient.getPreviousSecret());
    }
}
