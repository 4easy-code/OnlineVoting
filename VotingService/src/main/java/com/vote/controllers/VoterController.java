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
import com.vote.dto.CasteVoteDto;
import com.vote.exceptions.AlreadyVotedException;
import com.vote.exceptions.PartyNotFoundException;
import com.vote.exceptions.UnderAgeException;
import com.vote.exceptions.UpdateUserDetailsException;
import com.vote.exceptions.VoterNotFoundException;
import com.vote.response.ApiResponse;
import com.vote.response.CasteVoteResponse;
import com.vote.services.VoterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin("http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.VOTER)
public class VoterController {
    private final VoterService voterService;

    @PostMapping(ApiConstant.CAST_VOTE)
    public ResponseEntity<ApiResponse<CasteVoteResponse>> castVote(@RequestBody @Valid CasteVoteDto casteVoteDto) throws VoterNotFoundException, PartyNotFoundException, UnderAgeException, AlreadyVotedException, UpdateUserDetailsException {
        CasteVoteResponse response = voterService.castVote(casteVoteDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(response, "Success", "Vote cast successfully", HttpStatus.CREATED.value(), LocalDateTime.now()));
    }
}
