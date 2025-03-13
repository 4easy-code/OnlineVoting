package com.auth.services;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.auth.util.JwtUtil;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    
    private final JwtUtil jwtUtil;
    
    private Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    public String getUserKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String key;
        
        if (authHeader != null) {
            String JWTToken = authHeader.substring(7).trim();
            String userId = jwtUtil.extractUsername(JWTToken);
            key = (userId != null) ? "USER_" + userId : "IP_" + request.getRemoteAddr();
        } else {
        	logger.info("Fallback: Use IP address if user is not authenticated");
            key = "IP_" + request.getRemoteAddr();
        }

        logger.info("Generated rate limit key: {}", key);
        return key;
    }

    
    public synchronized Bucket resolveBucket(String key, int capacity, int refillTokens, int refillDuration) {
        Bucket bucket = userBuckets.computeIfAbsent(key, k -> {
            logger.info("Creating bucket for key: {}", key);
            Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(refillTokens, Duration.ofMinutes(refillDuration)));
            return Bucket4j.builder().addLimit(limit).build();
        });
        
        // this wasted my whole evening :)
        // Bucket4j uses a "greedy" refill strategy -- It gradually refills tokens over a the duration (duration / refill).
        // Refill.intervally will add all tokens back at once after duration

        logger.info("Available tokens BEFORE request for {}: {}", key, bucket.getAvailableTokens());
        return bucket;
    }
}
