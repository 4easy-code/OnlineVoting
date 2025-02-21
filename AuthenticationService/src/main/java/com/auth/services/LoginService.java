package com.auth.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth.dto.LoginDto;
import com.auth.dto.OtpDto;
import com.auth.dto.UserDto;
import com.auth.entities.Otp;
import com.auth.entities.User;
import com.auth.exceptions.InvalidCredentialsException;
import com.auth.exceptions.InvalidPasswordException;
import com.auth.exceptions.OtpGenerationFailedException;
import com.auth.exceptions.UserAlreadyExistsException;
import com.auth.exceptions.UserNotFoundException;
import com.auth.exceptions.UserNotValidatedException;
import com.auth.repos.OtpRepository;
import com.auth.repos.UserRepository;
import com.auth.response.JwtResponse;
import com.auth.response.OtpResponse;
import com.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {
	private final UserRepository userRepository;
	private final OtpRepository otpRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final TokenStore tokenStore;
	
	private final ModelMapper modelMapper;
	
	@Value("${app.jwt-expiration-milliseconds}")
	private Long jwtExpirationTime;
	
	
	private Logger logger = LoggerFactory.getLogger(LoginService.class);
	
	
	public UserDto registerUser(UserDto userDto) throws UserAlreadyExistsException {
		Optional<User> user = userRepository.findByUsernameOrEmail(userDto.getUsername(), userDto.getEmail());
		if(user.isPresent()) {
			throw new UserAlreadyExistsException(userDto.getUsername() + " or " + userDto.getEmail() + " already exists");
		}
		
		// if user already exists can change password
		// from front end, one can only register as a "USER"
		User newUser = modelMapper.map(userDto, User.class);
		newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
		
		User savedUser = userRepository.save(newUser);
		
		return modelMapper.map(savedUser, UserDto.class);
	}
	
	
	public JwtResponse loginUser(LoginDto loginDto) throws UserNotFoundException, InvalidPasswordException, InvalidCredentialsException, UserNotValidatedException {
		Optional<User> byUsernameOrEmail = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail());
		if(byUsernameOrEmail.isEmpty()) {
			throw new UserNotFoundException(loginDto.getUsernameOrEmail() + " is not registered. Kindly create account!");
		}
		if(byUsernameOrEmail.isPresent() && !byUsernameOrEmail.get().isVerified()) {
			throw new UserNotValidatedException("You need to verify, before you can login.");
		}
		
		User user = byUsernameOrEmail.get();
		if(!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
			throw new InvalidPasswordException("Password is incorrect !");
		}
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword())
				);
		
		if(authentication.isAuthenticated()) {
			String role = user.getRole();
			String username = user.getUsername();
			String token = jwtUtil.generateToken(username, role);
			
			tokenStore.storeToken(username, token, jwtExpirationTime / 1000);
			
			return new JwtResponse(token, username, role);
		} else {
			throw new InvalidCredentialsException("Invalid username or password!");
		}
	}
	
	
	public OtpResponse generateOtp(String usernameOrEmail) throws UserNotFoundException, OtpGenerationFailedException {
		Optional<User> byUsernameOrEmail = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
		if(byUsernameOrEmail.isEmpty()) {
			throw new UserNotFoundException(usernameOrEmail + " not found ");
		}
		
		List<Optional<Otp>> otpListOptional = otpRepository.findByUsernameoremail(usernameOrEmail);
		Optional<Otp> otpOptional =  (otpListOptional.isEmpty()) ? Optional.empty() : otpListOptional.get(otpListOptional.size() - 1);
		
		if(otpOptional.isPresent()) {
			if(otpOptional.get().getExpiresAt().isAfter(LocalDateTime.now())) {
				throw new OtpGenerationFailedException("can't generate new otp, as old is still valid! ");
			}
		}
		
		SecureRandom random = new SecureRandom();
		int otpGenerated = 1000 + random.nextInt(9000); // OTP always 4 digit
		
		Otp otp = new Otp();
		otp.setOtp(String.valueOf(otpGenerated));
		otp.setUsernameoremail(usernameOrEmail);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));  // OTP valid for 5 minutes
        
        otpRepository.save(otp);
        
        return modelMapper.map(otp, OtpResponse.class);
	}
	
	public boolean validateOtp(OtpDto otpDto) {
		logger.info("trying to validate OTP ");
		
		List<Optional<Otp>> otpListOptional = otpRepository.findByUsernameoremail(otpDto.getUsernameoremail());
		
		Otp otp = 
				(otpListOptional.isEmpty() == true) ? 
						null : otpListOptional.get(otpListOptional.size()-1).get();
		
		
		if(otp != null && (otp.getExpiresAt().isAfter(LocalDateTime.now()))) {
			if((otpDto.getOtp().equals(otp.getOtp()))) { // OTP in DB and opt by DTO is same or not
				Optional<User> byUsername = userRepository.findByUsername(otpDto.getUsernameoremail());
				Optional<User> byEmail = userRepository.findByEmail(otpDto.getUsernameoremail());
				
				// if otp is verified, user must be present
				User user = (byUsername.isPresent()) ? byUsername.get() : byEmail.get();
				user.setVerified(true);
				userRepository.save(user);
				
				otpRepository.delete(otp); // delete otp upon successful verification
				
				logger.info("OTP validatd !");
				
				return true;
			}
		}
		
		logger.info("OTP validation failed :");
		
		return false;
	}
}
