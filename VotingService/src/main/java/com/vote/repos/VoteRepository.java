package com.vote.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vote.entities.Party;
import com.vote.entities.Vote;
import com.vote.entities.Voter;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

	Integer countByParty(Party party);

	boolean existsByVoter(Voter voter);

}