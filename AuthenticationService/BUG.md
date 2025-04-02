### 
	## All tokens are invalidated during cleanup, even if valid.
	tokenStore.activeSessions.forEach((username, tokens) -> {
    tokens.removeIf(token -> !jwtUtil.validateToken(token, username));
	});
	username here is actually the map key (e.g., access_token:alice), not the actual username.
	jwtUtil.validateToken() expects the raw username (e.g., alice), not the prefixed key (as checking against username)

	## solution: remove prefix while sending to jwtUtil
	
	
