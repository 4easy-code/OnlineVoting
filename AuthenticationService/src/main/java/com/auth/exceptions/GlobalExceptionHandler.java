package com.auth.exceptions;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth.response.ErrorResponse;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler({InvalidCredentialsException.class,
	InvalidOtpException.class,
	InvalidPasswordException.class,
	OtpGenerationFailedException.class,
	UserAlreadyExistsException.class,
	UserNotFoundException.class,
	UserNotValidatedException.class,
	InvalidRefreshTokenException.class
	})
	public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
		logger.error("An error occured: {}", ex.getMessage());
		
		ErrorResponse errorResponse = new ErrorResponse(
				"Failure",
				ex.getMessage(),
				HttpStatus.CONFLICT.value(),
				LocalDateTime.now());
		
		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);
		
	}
	
	@ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
		ErrorResponse errorResponse = new ErrorResponse(
				"Rate Limit Exceeded",
				ex.getMessage(),
				HttpStatus.TOO_MANY_REQUESTS.value(),
				LocalDateTime.now());
		
		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("An error occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
    }
	
}
