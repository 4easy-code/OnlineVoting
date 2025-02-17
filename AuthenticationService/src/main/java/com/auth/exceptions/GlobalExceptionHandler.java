package com.auth.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth.response.ErrorResponse;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	
	@ExceptionHandler({InvalidCredentialsException.class,
	InvalidOtpException.class,
	InvalidPasswordException.class,
	OtpGenerationFailedException.class,
	UserAlreadyExistsException.class,
	UserNotFoundException.class,
	UserNotValidatedException.class
	})
	public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
		
		ErrorResponse errorResponse = new ErrorResponse("Failure",
				ex.getMessage(),
				HttpStatus.CONFLICT.value(),
				LocalDateTime.now());
		
		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);
		
	}
	
}
