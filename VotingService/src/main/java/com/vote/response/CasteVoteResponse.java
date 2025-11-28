package com.vote.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CasteVoteResponse {
	private Long voterId;
	private Long partyId;
	private Boolean hasVoted;
}