package com.vote.services;


import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.github.benmanes.caffeine.cache.Cache;
import com.vote.controllers.VotingResultController;
import com.vote.dto.RegisterVoteDto;
import com.vote.entities.Party;
import com.vote.entities.Vote;
import com.vote.entities.Voter;
import com.vote.exceptions.AlreadyVotedException;
import com.vote.exceptions.PartyNotFoundException;
import com.vote.exceptions.VoterNotFoundException;
import com.vote.repos.PartyRepository;
import com.vote.repos.VoteRepository;
import com.vote.repos.VoterRepository;
import com.vote.response.RegisterVoteResponse;
import com.vote.response.VotingResultResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteServices {
	
	private final VoteRepository voteRepository;
	private final VoterRepository voterRepository;
	private final PartyRepository partyRepository;
	private final VotingResultController votingResultController;
	private final Cache<String, VotingResultResponse> votingResultCache;
	private final ModelMapper mapper;
	
	private final Logger logger = LoggerFactory.getLogger(VoteServices.class);
	
	@Transactional
	public RegisterVoteResponse registerVote(RegisterVoteDto registerVoteDto) throws VoterNotFoundException, PartyNotFoundException, AlreadyVotedException {
		Voter voter = voterRepository.findByUsernameoremail(registerVoteDto.getUsernameoremail())
			.orElseThrow(() -> new VoterNotFoundException("Voter not found!!"));
		
		
		if(voteRepository.existsByVoter(voter)) {
			throw new AlreadyVotedException("Voter has already voted!");
		}
		
		if (voter.isHasVoted()) {
	        throw new AlreadyVotedException("Voter has already voted!");
	    }
		
		Party party = partyRepository.findById(registerVoteDto.getPartyId())
			.orElseThrow(() -> new PartyNotFoundException("Party with ID: " + registerVoteDto.getPartyId() + " not found !"));
		
		Vote vote = new Vote();
		vote.setParty(party);
		vote.setVoter(voter);
		vote.setTimeStamp(LocalDateTime.now());
		
		
		voter.setHasVoted(true);
		voter.setParty(party);

		voterRepository.save(voter);
		voteRepository.save(vote);
		
		TransactionSynchronizationManager.registerSynchronization(
		        new TransactionSynchronization() {
		            @Override
		            public void afterCommit() {
		            	votingResultCache.invalidate("current");
                        try {
							votingResultController.sendUpdatedResults();
						} catch (PartyNotFoundException e) {
							e.printStackTrace();
						}
		            }
		        }
		    );
		
		return mapper.map(registerVoteDto, RegisterVoteResponse.class);
	}
}
