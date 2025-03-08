package com.auth.exceptions;

public class InvalidRefreshTokenException extends Exception {
	public InvalidRefreshTokenException(String message) {
		super(message);
	}
}
