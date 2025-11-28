package com.vote.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "AUTH-SERVICE",contextId = "tokenStoreClient", url = "http://localhost:8081")
public interface TokenStoreClient   {
    @GetMapping("/votingapi/tokenStore/isAccessTokenValid")
    Boolean validateToken(@RequestParam("token") String token);
}
