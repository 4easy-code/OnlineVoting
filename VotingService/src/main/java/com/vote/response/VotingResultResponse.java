package com.vote.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VotingResultResponse {
	private List<String> winner;
	private List<String> runnerUps;
	private String result;
	private Integer totalVotesRegistered;
}
