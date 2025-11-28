package com.vote.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.benmanes.caffeine.cache.Cache;
import com.vote.entities.Party;
import com.vote.exceptions.PartyNotFoundException;
import com.vote.repos.PartyRepository;
import com.vote.repos.VoteRepository;
import com.vote.response.VotingResultResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VotingResultServices {
    private final PartyRepository partyRepository;
    private final VoteRepository voteRepository;
    private final Cache<String, VotingResultResponse> votingResultCache;
    
    private final Logger logger = LoggerFactory.getLogger(VotingResultServices.class);

    public VotingResultResponse publishVotingResult() throws PartyNotFoundException {
        // Check cache first
        VotingResultResponse cached = votingResultCache.getIfPresent("current");
        if (cached != null) {
            return cached;
        }

        // Cache miss - compute and store
        VotingResultResponse freshResult = computeFreshResult();
        votingResultCache.put("current", freshResult);
        return freshResult;
    }
    
    
    public VotingResultResponse computeFreshResult() throws PartyNotFoundException {
        logger.info("Working on publishing Voting Result...");

        List<Party> allParties = partyRepository.findAll();
        if (allParties.isEmpty()) {
            throw new PartyNotFoundException("No party exists.");
        }

        Map<Long, List<String>> voteCount = new TreeMap<>(Comparator.reverseOrder());
        for (Party party : allParties) {
            long cnt = party.getVotes().size();
            voteCount.computeIfAbsent(cnt, k -> new ArrayList<>()).add(party.getPartyName());
        }

        List<String> winners = new ArrayList<>();
        List<String> runnerUp = new ArrayList<>();

        for (Map.Entry<Long, List<String>> entry : voteCount.entrySet()) {
        	Long cnt = entry.getKey();
            List<String> parties = entry.getValue();
            
            if(cnt == 0) {
            	break;
            } else if (winners.isEmpty()) {
                winners.addAll(parties);
            } else if (runnerUp.isEmpty()) {
                runnerUp.addAll(parties);
                break;
            }
        }

        String result = "result can't be decided yet!";
        if (winners.size() == 1) {
            result = winners.get(0) + " won the election.";
            if (!runnerUp.isEmpty()) {
                result += " Runner-up: " + String.join(", ", runnerUp) + ".";
            }
        } else {
            result = "A coalition government will be formed by: " + String.join(", ", winners) + ".";
        }

        int totalVotes = (int) voteRepository.count();

        return new VotingResultResponse(winners, runnerUp, result, totalVotes);
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    @Transactional
    public void precomputeResults() {
        try {
            votingResultCache.put("current", computeFreshResult());
        } catch (PartyNotFoundException e) {
            logger.error("Failed to precompute results", e);
        }
    }
}
