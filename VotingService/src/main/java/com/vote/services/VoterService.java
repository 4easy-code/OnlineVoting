package com.vote.services;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.vote.dto.CasteVoteDto;
import com.vote.dto.RegisterVoteDto;
import com.vote.dto.UserDto;
import com.vote.entities.Voter;
import com.vote.exceptions.AlreadyVotedException;
import com.vote.exceptions.PartyNotFoundException;
import com.vote.exceptions.UnderAgeException;
import com.vote.exceptions.UpdateUserDetailsException;
import com.vote.exceptions.VoterNotFoundException;
import com.vote.feign.UserClient;
import com.vote.repos.VoterRepository;
import com.vote.response.CasteVoteResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoterService {
	private final UserClient userClient;
	private final ModelMapper mapper;
	private final VoteServices voteServices;
	private final VoterRepository voterRepository;
	
	public CasteVoteResponse castVote(CasteVoteDto casteVoteDto) throws VoterNotFoundException, PartyNotFoundException, UnderAgeException, AlreadyVotedException, UpdateUserDetailsException {
		UserDto userDetails = userClient.getUserDetails(casteVoteDto.getUsernameoremail()).getBody().getData();
				
		if(userDetails.getAge() == null) {
			throw new UpdateUserDetailsException("Check if you have updated your age!!");
		}
		
		if(userDetails.getAge() < 18) {
			throw new UnderAgeException("Voter is below 18 year !");
		}
		
		Voter voter = voterRepository.findByUsernameoremail(casteVoteDto.getUsernameoremail())
			    .orElseGet(() -> {
			        Voter newVoter = new Voter();
			        newVoter.setUsernameoremail(userDetails.getUsername());
			        newVoter.setAge(userDetails.getAge());
			        newVoter.setHasVoted(false);
			        return voterRepository.save(newVoter);
			    });
	    
		voteServices.registerVote(mapper.map(casteVoteDto, RegisterVoteDto.class));
		
		return new CasteVoteResponse(voter.getVoterId(), casteVoteDto.getPartyId(), true);
	}
	
}
