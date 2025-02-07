package com.auth.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Otp {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long otpId;
	private String usernameoremail;
	private String otp;
	private LocalDateTime createdAt;
	private LocalDateTime expiresAt;	
}
