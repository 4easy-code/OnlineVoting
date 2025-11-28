
package com.vote.exceptions;

public class AlreadyVotedException extends Exception {
	public AlreadyVotedException(String message) {
		super(message);
	}
}