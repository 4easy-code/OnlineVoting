package com.auth.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.auth.services.TokenStore;
import com.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final TokenStore tokenStore;
    private final JwtUtil jwtUtil;
    
    private Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    @Scheduled(fixedRate = 120000) // run every 2 minute to clean expired tokens
    public void cleanExpiredTokens() {
        logger.info("Running expired token cleanup...");
        tokenStore.activeSessions.forEach((username, tokens) -> {
            tokens.removeIf(token -> !jwtUtil.validateToken(token, username));
        });
    }
}