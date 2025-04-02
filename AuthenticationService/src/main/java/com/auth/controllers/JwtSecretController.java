package com.auth.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.constant.ApiConstant;
import com.auth.services.JwtSecretService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstant.JWT_SECRET)
@RequiredArgsConstructor
public class JwtSecretController {
    private final JwtSecretService jwtSecretService;
    private Logger logger = LoggerFactory.getLogger(JwtSecretController.class);

    @GetMapping(ApiConstant.CURRENT_SECRET)
    public ResponseEntity<String> getCurrentSecret() {
    	logger.info("tyring to get current jwt secret ... through feign");
        return ResponseEntity.status(HttpStatus.OK)
        		.body(jwtSecretService.getCurrentSecret());
    }

    @GetMapping(ApiConstant.PREVIOUS_SECRET)
    public ResponseEntity<String> getPreviousSecret() {
    	logger.info("tyring to get previous jwt secret ... through feign");
        return ResponseEntity.status(HttpStatus.OK)
        		.body(jwtSecretService.getPreviousSecret());
    }
}
