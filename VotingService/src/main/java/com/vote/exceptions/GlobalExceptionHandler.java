package com.vote.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vote.response.ErrorResponse;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	
	@ExceptionHandler({AlreadyVotedException.class,
		PartyAlreadyRegisteredException.class,
		PartyNotFoundException.class,
		UnderAgeException.class,
		VoterNotFoundException.class,
		UpdateUserDetailsException.class,
		TokenPassedToSSEIsExpired.class
	})
	public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
		
		ErrorResponse errorResponse = new ErrorResponse("Failure",
				ex.getMessage(),
				HttpStatus.CONFLICT.value(),
				LocalDateTime.now());
		
		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);
		
	}

}