package com.auth.exceptions;

public class InvalidPasswordException extends Exception {
	public InvalidPasswordException(String message) {
		super(message);
	}
}