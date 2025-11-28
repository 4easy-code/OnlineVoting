package com.vote.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllPartyVotes {
	private Map<String, Integer> allPartyVoteCount;
}
