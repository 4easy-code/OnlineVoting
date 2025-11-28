package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatePartyDto {
	private String partyName;
	private String partySymbol;
	private String candidateName;
}
