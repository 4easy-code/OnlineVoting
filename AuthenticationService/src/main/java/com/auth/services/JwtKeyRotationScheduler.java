package com.auth.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtKeyRotationScheduler {
	private final JwtSecretService jwtSecretService;
	
	@Scheduled(fixedRate = 1800000) // rotate every 30 minutes -- 1800000
    public void rotateKeysDaily() {
        jwtSecretService.rotateSecret();
    }
}
