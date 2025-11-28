package com.vote.exceptions;

public class TokenPassedToSSEIsExpired extends Exception {
	public TokenPassedToSSEIsExpired(String message) {
		super(message);
	}
}
