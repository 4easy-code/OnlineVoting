package com.vote.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vote.feign.JwtSecretClient;
import com.vote.response.VotingResultResponse;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CacheConfig {
	private final JwtSecretClient jwtSecretClient;

    @Bean
    LoadingCache<String, String> jwtSecretCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // Auto-delete after 10 minutes
                .refreshAfterWrite(9, TimeUnit.MINUTES)  // Refresh in the background
                .maximumSize(2)  // Store only "current" and "previous" secrets
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        if ("current".equals(key)) {
                            return jwtSecretClient.getCurrentSecret();
                        } else if ("previous".equals(key)) {
                            return jwtSecretClient.getPreviousSecret();
                        } else {
                            throw new IllegalArgumentException("Unknown key: " + key);
                        }
                    }
                });
    }
    
    @Bean
    Cache<String, VotingResultResponse> votingResultCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES) // Refresh every minute - database call
                .maximumSize(1) // Only store latest result
                .build();
    }
}
