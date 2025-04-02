package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private String username;
	private String password;
	private String email;
	private String role;
	
	private String gender;
	private String phoneNumber;
	private String country;
	private Integer age;

	private boolean isVerified;
}