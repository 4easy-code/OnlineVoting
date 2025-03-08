package com.auth.services;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.auth.dto.ChangePasswordDto;
import com.auth.dto.DeleteUserDto;
import com.auth.dto.UpdateUserDto;
import com.auth.dto.UserDto;
import com.auth.entities.Otp;
import com.auth.entities.User;
import com.auth.exceptions.InvalidOtpException;
import com.auth.exceptions.InvalidPasswordException;
import com.auth.exceptions.OtpGenerationFailedException;
import com.auth.exceptions.UserNotFoundException;
import com.auth.repos.OtpRepository;
import com.auth.repos.UserRepository;
import com.auth.response.UpdateUserResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final OtpRepository otpRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	
	private Logger logger = LoggerFactory.getLogger(UserService.class);
	
	
	public UserDto getUserDetails(String usernameOrEmail) throws UserNotFoundException {
	    logger.info("Trying to get User details");

	    // Get the authenticated user's details
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    
	    if (authentication == null || !authentication.isAuthenticated()) {
	        throw new AccessDeniedException("Unauthorized request");
	    }

	    String authenticatedUsername = authentication.getName();
	    boolean isAdmin = authentication.getAuthorities().stream()
	        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

	    // If the user is NOT an admin, they should only fetch their own details
	    if (!isAdmin && !authenticatedUsername.equals(usernameOrEmail)) {
	        throw new AccessDeniedException("Access denied. You can only view your own details.");
	    }

	    Optional<User> user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
	    if (user.isEmpty()) {
	        throw new UserNotFoundException(usernameOrEmail + " not present!");
	    }

	    logger.info("User details fetched successfully");

	    return modelMapper.map(user.get(), UserDto.class);
	}

	
	public Boolean changePassword(ChangePasswordDto passwordDto) throws UserNotFoundException, OtpGenerationFailedException, InvalidOtpException, InvalidPasswordException {
		List<Optional<Otp>> otpListOptional = otpRepository.findByUsernameoremail(passwordDto.getUsernameOrEmail());
		if(otpListOptional.isEmpty()) {
			throw new InvalidOtpException("OTP not present for " + passwordDto.getUsernameOrEmail());
		}
		Optional<Otp> otpOptional = otpListOptional.get(otpListOptional.size()-1);
		
		Otp otp = otpOptional.get();
		if(!otp.getOtp().equals(passwordDto.getOtp())) {
			throw new InvalidOtpException("Invalid OTP");
		}
		
		Optional<User> userOptional = userRepository.findByUsernameOrEmail(passwordDto.getUsernameOrEmail(), passwordDto.getUsernameOrEmail());
		if(userOptional.isEmpty()) {
			throw new UserNotFoundException("User not present in database");
		}
		
		User user = userOptional.get();
		
		if(user.getPassword().matches(passwordDto.getPassword())) {
			throw new InvalidPasswordException("new password can't be same as old password");
		}
		
		user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
		userRepository.save(user);
		
		otpRepository.delete(otp); // delete OTP upon use
		return true;
	}
	
	
	public UpdateUserResponse updateUserDetails(UpdateUserDto updateUserDto) throws UserNotFoundException {
		logger.info("trying to update user");
		
		Optional<User> userOptional = userRepository.findByUsernameOrEmail(updateUserDto.getUsernameOrEmail(), updateUserDto.getUsernameOrEmail());
		if(userOptional.isEmpty()) {
			throw new UserNotFoundException("User not present! ");
		}
		
		User user = userOptional.get();
		user.setCountry(updateUserDto.getCountry());
		user.setGender(updateUserDto.getGender());
		user.setPhoneNumber(updateUserDto.getPhoneNumber());
		userRepository.save(user);
		
		return modelMapper.map(user, UpdateUserResponse.class);
	}
	
	public DeleteUserDto deleteUser(DeleteUserDto deleteUserDto) throws UserNotFoundException {
		Optional<User> user = userRepository.findByUsername(deleteUserDto.getUsername());
		if(user.isEmpty()) {
			throw new UserNotFoundException(deleteUserDto.getUsername() + "User not present in database");
		}
		
		userRepository.delete(user.get());
		return deleteUserDto;
	}
}
