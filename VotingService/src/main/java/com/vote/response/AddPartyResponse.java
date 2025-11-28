package com.vote.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPartyResponse {
	private String partyName;
	private String partySymbol;
	private String candidateName;
}
