package com.auth.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.constant.ApiConstant;
import com.auth.services.TokenStore;
import com.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstant.TOKEN_STORE)
@RequiredArgsConstructor
public class TokenStoreController {

    private final TokenStore tokenStore;
    private final JwtUtil jwtUtil;
    
    private Logger logger = LoggerFactory.getLogger(TokenStoreController.class);

    @GetMapping(ApiConstant.IS_ACCESS_TOKEN_VALID)
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
    	logger.info("calling token-store through feign");
    	String username = jwtUtil.extractUsername(token);
    	logger.info(token);
    	logger.info(username);
        boolean isValid = tokenStore.isTokenValid(username, token);
        return ResponseEntity.status(HttpStatus.OK)
        		.body(isValid);
    }
}