package com.auth.controllers;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.constant.ApiConstant;
import com.auth.dto.RefreshTokenRequestDto;
import com.auth.exceptions.InvalidRefreshTokenException;
import com.auth.response.ApiResponse;
import com.auth.response.JwtResponse;
import com.auth.services.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.REFRESH)
public class RefreshTokenController {
	
	private final RefreshTokenService refreshTokenService;
	
	@PostMapping(ApiConstant.CREATE_ACCESSTOKEN)
	public ResponseEntity<ApiResponse<JwtResponse>> createAccessToken(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) throws InvalidRefreshTokenException {
		JwtResponse accessToken = refreshTokenService.createAccessToken(refreshTokenRequestDto);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse<>(accessToken,  "Created", "Access Token created successfully", HttpStatus.CREATED.value(), LocalDateTime.now()));
	}
}
