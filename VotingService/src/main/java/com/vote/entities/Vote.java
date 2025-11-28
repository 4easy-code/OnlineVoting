package com.vote.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteId;
    private LocalDateTime timeStamp;

    
    @ToString.Exclude
    @JsonBackReference("party-votes")
    @ManyToOne
    @JoinColumn(name = "party_id", referencedColumnName = "partyId")
    private Party party;
    
    @ToString.Exclude
    @JsonManagedReference("vote-voter")
    @OneToOne
    @JoinColumn(name = "voter_id", referencedColumnName = "voterId", unique = true)
    private Voter voter;
}