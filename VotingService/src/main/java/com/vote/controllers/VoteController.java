package com.vote.controllers;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vote.constant.ApiConstant;
import com.vote.dto.RegisterVoteDto;
import com.vote.exceptions.AlreadyVotedException;
import com.vote.exceptions.PartyNotFoundException;
import com.vote.exceptions.VoterNotFoundException;
import com.vote.response.ApiResponse;
import com.vote.response.RegisterVoteResponse;
import com.vote.services.VoteServices;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin("http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.VOTE)
public class VoteController {
    private final VoteServices voteServices;

    @PostMapping(ApiConstant.REGISTER_VOTE)
    public ResponseEntity<ApiResponse<RegisterVoteResponse>> registerVote(@RequestBody @Valid RegisterVoteDto registerVoteDto) throws VoterNotFoundException, PartyNotFoundException, AlreadyVotedException {
        RegisterVoteResponse response = voteServices.registerVote(registerVoteDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(response, "Success", "Vote registered successfully", HttpStatus.CREATED.value(), LocalDateTime.now()));
    }
}
