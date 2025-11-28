package com.vote.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vote.entities.Party;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long>{

	Optional<Party> findByPartyName(String partyName);

}
