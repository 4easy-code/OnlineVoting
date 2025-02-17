package com.auth.controllers;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.constant.ApiConstant;
import com.auth.dto.LoginDto;
import com.auth.dto.OtpDto;
import com.auth.dto.UserDto;
import com.auth.exceptions.InvalidCredentialsException;
import com.auth.exceptions.InvalidPasswordException;
import com.auth.exceptions.OtpGenerationFailedException;
import com.auth.exceptions.UserAlreadyExistsException;
import com.auth.exceptions.UserNotFoundException;
import com.auth.exceptions.UserNotValidatedException;
import com.auth.response.ApiResponse;
import com.auth.response.JwtResponse;
import com.auth.response.OtpResponse;
import com.auth.services.LoginService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
@RequestMapping(ApiConstant.AUTH)
public class AuthController {
	
	private final LoginService loginService;
	
	private Logger logger = LoggerFactory.getLogger(AuthController.class);
	
	@PostMapping(ApiConstant.CREATE_USER)
	public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody @Valid UserDto userDto) throws UserAlreadyExistsException {
		UserDto user = loginService.registerUser(userDto);
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse<>(user, "Success", "User added successfully", HttpStatus.CREATED.value(), LocalDateTime.now()));
	}
	
	@PostMapping(ApiConstant.LOGIN)
	public ResponseEntity<ApiResponse<JwtResponse>> loginUser(@RequestBody @Valid LoginDto loginDto) throws UserNotFoundException, InvalidPasswordException, InvalidCredentialsException, UserNotValidatedException {
		
		
		JwtResponse jwtResponse = loginService.loginUser(loginDto);
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse<>(jwtResponse, "Success", "User Logged in successfully", HttpStatus.OK.value(), LocalDateTime.now()));
	}
	
	@PostMapping(ApiConstant.CREATE_OTP)
	public ResponseEntity<ApiResponse<OtpResponse>> generateOtp(@PathVariable String userToken) throws UserNotFoundException, OtpGenerationFailedException {
		OtpResponse otp = loginService.generateOtp(userToken);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse<>(otp, "Success", "otp created successfully", HttpStatus.CREATED.value(), LocalDateTime.now()));
	}
	
	@PostMapping(ApiConstant.VALIDATE_USER)
	public ResponseEntity<ApiResponse<Boolean>> validateOtp(@RequestBody @Valid OtpDto otpDto) {
		boolean isOtpValid = loginService.validateOtp(otpDto);
		if(isOtpValid) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse<>(isOtpValid, "Success", "otp validated successfully", HttpStatus.OK.value(), LocalDateTime.now()));
		}
		return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY)
				.body(new ApiResponse<>(isOtpValid, "Failed", "otp validation failed", HttpStatus.FAILED_DEPENDENCY.value(), LocalDateTime.now()));
	}
	
		
}