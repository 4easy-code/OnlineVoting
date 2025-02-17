package com.auth.services;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenStore {
	private final ConcurrentHashMap<String, String> activeTokens = new ConcurrentHashMap<>();

    public void storeToken(String username, String token) {
        activeTokens.put(username, token);
    }

    public boolean isTokenValid(String username, String token) {
        return token.equals(activeTokens.get(username));
    }

    public void removeToken(String username) {
        activeTokens.remove(username);
    }
}
