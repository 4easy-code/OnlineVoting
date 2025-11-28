package com.vote.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
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
public class Voter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voterId;
    
    private Integer age;
    private String usernameoremail;
    
    private boolean hasVoted = false;
    
    @ToString.Exclude
    @JsonBackReference("vote-voter")
    @OneToOne(mappedBy = "voter", cascade = CascadeType.ALL)
    private Vote vote;
    
    @ToString.Exclude
    @JsonBackReference("party-voters")
    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;
}