package com.vote.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "AUTH-SERVICE", contextId = "jwtSecretClient", url = "http://localhost:8081")
public interface JwtSecretClient {
    
    @GetMapping("/votingapi/jwtSecret/current")
    String getCurrentSecret();
    
    @GetMapping("/votingapi/jwtSecret/previous")
    String getPreviousSecret();
}
