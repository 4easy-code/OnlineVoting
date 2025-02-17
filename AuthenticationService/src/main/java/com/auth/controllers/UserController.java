package com.auth.controllers;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.constant.ApiConstant;
import com.auth.dto.ChangePasswordDto;
import com.auth.dto.DeleteUserDto;
import com.auth.dto.UpdateUserDto;
import com.auth.dto.UserDto;
import com.auth.exceptions.InvalidOtpException;
import com.auth.exceptions.OtpGenerationFailedException;
import com.auth.exceptions.UserNotFoundException;
import com.auth.response.ApiResponse;
import com.auth.response.UpdateUserResponse;
import com.auth.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
@RequestMapping(ApiConstant.USERS)
public class UserController {
	private final UserService userService;
	
    private Logger logger = LoggerFactory.getLogger(UserController.class);

	@GetMapping(ApiConstant.GET_USER_DETAILS)
	public ResponseEntity<ApiResponse<UserDto>> getUserDetails(@PathVariable String usernameOrEmail) throws UserNotFoundException {
		
		logger.info("Calling get user details api");
		UserDto userDetails = userService.getUserDetails(usernameOrEmail);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse<>(userDetails, "Success", "User Details fetched successfully", HttpStatus.OK.value(), LocalDateTime.now()));
	}
	
	@PutMapping(ApiConstant.CHANGE_PASSWORD)
	public ResponseEntity<ApiResponse<Boolean>> changePassword(@RequestBody ChangePasswordDto passwordDto) throws UserNotFoundException, OtpGenerationFailedException, InvalidOtpException {
		Boolean isPasswordChanged = userService.changePassword(passwordDto);
		if(!isPasswordChanged) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ApiResponse<>(false, "failed", "Password change failed", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now()));
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse<>(true, "Success", "Password changed successfully", HttpStatus.OK.value(), LocalDateTime.now()));
	}
	
	@PutMapping(ApiConstant.UPDATE)
	public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(@RequestBody UpdateUserDto updateUserDto) throws UserNotFoundException {
		logger.info("In update controller");
		UpdateUserResponse updatedUserDetails = userService.updateUserDetails(updateUserDto);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse<>(updatedUserDetails, "Success", "User updated successfully", HttpStatus.CREATED.value(), LocalDateTime.now()));
	}
	
	@DeleteMapping(ApiConstant.DELETE)
	public ResponseEntity<ApiResponse<DeleteUserDto>> deleteUser(@RequestBody DeleteUserDto deleteUserDto) throws UserNotFoundException {
		DeleteUserDto deletedUser = userService.deleteUser(deleteUserDto);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse<>(deletedUser, "Success", "User deleted successfully", HttpStatus.OK.value(), LocalDateTime.now()));
	}
}