package com.vote.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.vote.dto.UserDto;
import com.vote.response.ApiResponse;

@FeignClient(name = "AUTH-SERVICE",contextId = "userClient", url = "http://localhost:8081")
public interface UserClient {
	@GetMapping("/votingapi/users/getUserDetails/{usernameOrEmail}")
	public ResponseEntity<ApiResponse<UserDto>> getUserDetails(@PathVariable String usernameOrEmail);
}
