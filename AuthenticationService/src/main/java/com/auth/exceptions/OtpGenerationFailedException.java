package com.auth.exceptions;

public class OtpGenerationFailedException extends Exception {
	public OtpGenerationFailedException(String message) {
		super(message);
	}
}