package com.vote.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vote.constant.ApiConstant;
import com.vote.dto.AddPartyDto;
import com.vote.dto.DeletePartyDto;
import com.vote.dto.GetAllVotesOrVoterForPartyDto;
import com.vote.dto.UpdatePartyDto;
import com.vote.entities.Party;
import com.vote.entities.Vote;
import com.vote.entities.Voter;
import com.vote.exceptions.PartyAlreadyRegisteredException;
import com.vote.exceptions.PartyNotFoundException;
import com.vote.response.AddPartyResponse;
import com.vote.response.AllPartyVotes;
import com.vote.response.ApiResponse;
import com.vote.response.UpdatePartyResponse;
import com.vote.services.PartyServices;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin("http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.PARTY)
public class PartyController {
	private final PartyServices partyServices;
	
	private Logger logger = LoggerFactory.getLogger(PartyController.class);
	
	
	@PostMapping(ApiConstant.CREATE_PARTY)
	public ResponseEntity<ApiResponse<AddPartyResponse>> addParty(@RequestBody @Valid AddPartyDto addPartyDto) throws PartyAlreadyRegisteredException {
		
		logger.info("create party api called ...");
		
		AddPartyResponse partyResponse = partyServices.addParty(addPartyDto);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse<>(partyResponse, "Success", "Party added successfully", HttpStatus.CREATED.value(), LocalDateTime.now()));
	}
	
	@GetMapping(ApiConstant.GET_ALL_PARTIES)
    public ResponseEntity<ApiResponse<List<Party>>> getAllParties() {
        logger.info("Get all parties API called...");
        List<Party> parties = partyServices.getAllParties();
        return ResponseEntity.ok(new ApiResponse<>(parties, "Success", "Fetched all parties", HttpStatus.OK.value(), LocalDateTime.now()));
    }

    @PutMapping(ApiConstant.UPDATE_PARTY)
    public ResponseEntity<ApiResponse<UpdatePartyResponse>> updateParty(@RequestBody @Valid UpdatePartyDto updatePartyDto) throws PartyNotFoundException {
        logger.info("Update party API called...");
        UpdatePartyResponse updatedParty = partyServices.updateParty(updatePartyDto);
        return ResponseEntity.ok(new ApiResponse<>(updatedParty, "Success", "Party updated successfully", HttpStatus.OK.value(), LocalDateTime.now()));
    }

    @DeleteMapping(ApiConstant.DELETE_PARTY)
    public ResponseEntity<ApiResponse<String>> deleteParty(@RequestBody @Valid DeletePartyDto deletePartyDto) throws PartyNotFoundException {
        logger.info("Delete party API called...");
        partyServices.deleteParty(deletePartyDto);
        return ResponseEntity.ok(new ApiResponse<>("Party deleted successfully", "Success", "Party removed", HttpStatus.OK.value(), LocalDateTime.now()));
    }
    
    @DeleteMapping(ApiConstant.DELETE_ALL_PARTIES)
    public ResponseEntity<ApiResponse<String>> deleteAllParties() {
        logger.info("Delete all parties API called...");
        partyServices.deleteAllParties();
        return ResponseEntity.ok(new ApiResponse<>("All parties deleted successfully", "Success", "All parties removed", HttpStatus.OK.value(), LocalDateTime.now()));
    }

    @GetMapping(ApiConstant.GET_ALL_VOTES_FOR_PARTY)
    public ResponseEntity<ApiResponse<List<Vote>>> getAllVotesForParty(@RequestBody @Valid GetAllVotesOrVoterForPartyDto dto) throws PartyNotFoundException {
        logger.info("Get all votes for party API called...");
        List<Vote> votes = partyServices.getAllVotesForParty(dto);
        return ResponseEntity.ok(new ApiResponse<>(votes, "Success", "Fetched votes for party", HttpStatus.OK.value(), LocalDateTime.now()));
    }

    @GetMapping(ApiConstant.GET_ALL_VOTERS_FOR_PARTY)
    public ResponseEntity<ApiResponse<List<Voter>>> getAllVotersForParty(@RequestBody @Valid GetAllVotesOrVoterForPartyDto dto) throws PartyNotFoundException {
        logger.info("Get all voters for party API called...");
        List<Voter> voters = partyServices.getAllVotersForParty(dto);
        return ResponseEntity.ok(new ApiResponse<>(voters, "Success", "Fetched voters for party", HttpStatus.OK.value(), LocalDateTime.now()));
    }

    @GetMapping(ApiConstant.COUNT_VOTES_FOR_PARTY)
    public ResponseEntity<ApiResponse<Integer>> countVotesForParty(@RequestBody @Valid GetAllVotesOrVoterForPartyDto dto) throws PartyNotFoundException {
        logger.info("Count votes for party API called...");
        Integer voteCount = partyServices.countVotesForParty(dto);
        return ResponseEntity.ok(new ApiResponse<>(voteCount, "Success", "Vote count fetched", HttpStatus.OK.value(), LocalDateTime.now()));
    }

    @GetMapping(ApiConstant.GET_VOTE_COUNT_FOR_ALL_PARTIES)
    public ResponseEntity<ApiResponse<AllPartyVotes>> getVoteCountForAllParties() {
        logger.info("Get vote count for all parties API called...");
        AllPartyVotes votes = partyServices.getVoteCountForAllParties();
        return ResponseEntity.ok(new ApiResponse<>(votes, "Success", "Fetched vote count for all parties", HttpStatus.OK.value(), LocalDateTime.now()));
    }
}