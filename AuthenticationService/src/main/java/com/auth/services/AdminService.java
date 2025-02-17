package com.auth.services;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.RestController;

import com.auth.entities.Otp;
import com.auth.repos.OtpRepository;
import com.auth.repos.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminService {
	private UserRepository userRepository;
	private OtpRepository otpRepository;
	
	public Boolean deleteUnusedOtp(String usernameoremail, String otp) {
		List<Optional<Otp>> byUsernameoremailList = otpRepository.findByUsernameoremail(usernameoremail);
		Optional<Otp> otpOptional = byUsernameoremailList.get(byUsernameoremailList.size()-1);
		if(otpOptional.get().getOtp().equals(otp)) {
			return true;
		}
		return false;
	}
}
