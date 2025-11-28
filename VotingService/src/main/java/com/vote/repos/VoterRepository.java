package com.vote.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vote.entities.Voter;

@Repository
public interface VoterRepository extends JpaRepository<Voter, Long> {
	Optional<Voter> findByUsernameoremail(String usernameoremail);
}
