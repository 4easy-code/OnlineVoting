package com.vote.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.vote.dto.AddPartyDto;
import com.vote.dto.DeletePartyDto;
import com.vote.dto.GetAllVotesOrVoterForPartyDto;
import com.vote.dto.UpdatePartyDto;
import com.vote.entities.Party;
import com.vote.entities.Vote;
import com.vote.entities.Voter;
import com.vote.exceptions.PartyAlreadyRegisteredException;
import com.vote.exceptions.PartyNotFoundException;
import com.vote.repos.PartyRepository;
import com.vote.repos.VoteRepository;
import com.vote.response.AddPartyResponse;
import com.vote.response.AllPartyVotes;
import com.vote.response.UpdatePartyResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartyServices {
	
	private final PartyRepository partyRepository;
	private final VoteRepository voteRepository;
	private final ModelMapper mapper;
	
	public AddPartyResponse addParty(AddPartyDto partyDto) throws PartyAlreadyRegisteredException {
		
		Optional<Party> partyOptional = partyRepository.findByPartyName(partyDto.getPartyName());
		if(partyOptional.isPresent()) {
			throw new PartyAlreadyRegisteredException(partyDto.getPartyName() + " is already registered!!");
		}
		
		Party party = mapper.map(partyDto, Party.class);
		partyRepository.save(party);
		return mapper.map(partyDto, AddPartyResponse.class);
	}
	
	public List<Party> getAllParties() {
		return partyRepository.findAll();
	}
	
	public UpdatePartyResponse updateParty(UpdatePartyDto updatePartyDto) throws PartyNotFoundException {
		Party party = partyRepository.findByPartyName(updatePartyDto.getPartyName())
			.orElseThrow(() -> new PartyNotFoundException("Party not found!!"));
		
		
		if(updatePartyDto.getCandidateName()!= null) {
			party.setCandidateName(updatePartyDto.getCandidateName());
		}
		if(updatePartyDto.getPartySymbol() != null) {
			party.setPartySymbol(updatePartyDto.getPartySymbol());
		}
		partyRepository.save(party);
		
		return mapper.map(party, UpdatePartyResponse.class);
	}
	
	public void deleteParty(DeletePartyDto deletePartyDto) throws PartyNotFoundException {
		Optional<Party> party = partyRepository.findById(deletePartyDto.getPartyId());
		if(party.isEmpty()) {
			throw new PartyNotFoundException("Party with ID: " + deletePartyDto.getPartyId() + " is not present!");
		}
		partyRepository.delete(party.get());
	}
	
	public void deleteAllParties() {
		partyRepository.deleteAll();
	}
	
	
	public List<Vote> getAllVotesForParty(GetAllVotesOrVoterForPartyDto allVotesOrVoterForPartyDto) throws PartyNotFoundException {
		Optional<Party> partyOptional = partyRepository.findById(allVotesOrVoterForPartyDto.getPartyId());
		if(partyOptional.isEmpty()) {
			throw new PartyNotFoundException("Party with ID: " + allVotesOrVoterForPartyDto.getPartyId() + " not found! ");
		}
		return partyOptional.get().getVotes();
	}
	
	public List<Voter> getAllVotersForParty(GetAllVotesOrVoterForPartyDto allVotesOrVoterForPartyDto) throws PartyNotFoundException {
		Optional<Party> partyOptional = partyRepository.findById(allVotesOrVoterForPartyDto.getPartyId());
		if(partyOptional.isEmpty()) {
			throw new PartyNotFoundException("Party with ID: " + allVotesOrVoterForPartyDto.getPartyId() + " not found! ");
		}
		return partyOptional.get().getVoters();
	}
	
	public Integer countVotesForParty(GetAllVotesOrVoterForPartyDto allVotesOrVoterForPartyDto) throws PartyNotFoundException {
		Optional<Party> partyOptional = partyRepository.findById(allVotesOrVoterForPartyDto.getPartyId());
		if(partyOptional.isEmpty()) {
			throw new PartyNotFoundException("Party with ID: " + allVotesOrVoterForPartyDto.getPartyId() + " not found! ");
		}
		return voteRepository.countByParty(partyOptional.get());
	}
	
	public AllPartyVotes getVoteCountForAllParties() {
		Map<String, Integer> votesCount = partyRepository.findAll()
			    .stream()
			    .collect(Collectors.toMap(
			        Party::getPartyName, 
			        party -> party.getVotes().size()
			    ));
		
		return new AllPartyVotes(votesCount);
	}
}


