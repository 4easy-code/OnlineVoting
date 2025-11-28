package com.vote.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatePartyResponse {
	private String partyName;
	private String partySymbol;
	private String candidateName;
}
