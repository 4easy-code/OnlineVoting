package com.vote.dto;

import java.util.List;

import com.vote.entities.Party;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VotingResult {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long votingResultId;
	private List<Party> winnerParty;
	private List<Party> runnerParty;
}