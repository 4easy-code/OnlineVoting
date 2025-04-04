package com.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
	private String accessToken;
	private String refreshToken;
	private String username;
	private String role;
	private String email;
}
