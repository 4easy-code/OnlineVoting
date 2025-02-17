package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
	private String usernameOrEmail;
	private String gender;
	private String phoneNumber;
	private String country;
}
