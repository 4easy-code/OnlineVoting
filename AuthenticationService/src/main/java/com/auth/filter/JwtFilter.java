package com.auth.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth.services.CustomUserDetailsService;
import com.auth.services.TokenStore;
import com.auth.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService userDetailsService;
	private final TokenStore tokenStore;
	
	private Logger logger = LoggerFactory.getLogger(JwtFilter.class);
	
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
	    String path = request.getRequestURI();
	    return path.equals("/votingapi/auth/login")
	        || path.equals("/votingapi/auth/createUser")
	        || path.equals("/votingapi/auth/validateUser")
	        || path.startsWith("/votingapi/auth/createOtp");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {

	    logger.info("Trying to validate token: {}", request.getHeader("Authorization"));

	    String authHeader = request.getHeader("Authorization");
	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        filterChain.doFilter(request, response);
	        return;
	    }

	    String token = authHeader.substring(7);

	    try {
	        String username = jwtUtil.extractUsername(token);

	        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

	            logger.info("Extracted Role from UserDetails: {}", userDetails.getAuthorities());
                logger.info("Is token validated by JWTUtil: {}", jwtUtil.validateToken(token, userDetails.getUsername()));
                logger.info("Is token validated by Tokenstore: {}", (tokenStore.isValidToken(username, token) || tokenStore.isRefreshTokenValid(username, token) ));

	            if (jwtUtil.validateToken(token, userDetails.getUsername())) {
	            	String tokenType = jwtUtil.extractAllClaims(token).get("tokenType", String.class);
	            	if ("REFRESH".equals(tokenType)) {
	                    // Allow refresh token ONLY for the refresh endpoint
	                    if (!request.getRequestURI().equals("/api/auth/refresh-token")) {
	                        logger.warn("Refresh token cannot be used for authentication outside refresh endpoint");
	                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	                        response.getWriter().write("Refresh tokens cannot be used to access protected resources.");
	                        return;
	                    }
	                } else {
	                	// Validate only access tokens for normal authentication
	                    if (!tokenStore.isValidToken(username, token)) {
	                        logger.warn("Access token is invalid or expired");
	                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	                        response.getWriter().write("Session expired or invalid token");
	                        return;
	                    }
	                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
	                    		userDetails, null, userDetails.getAuthorities());
	                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                    SecurityContextHolder.getContext().setAuthentication(authToken);
	                }
	                logger.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
	            } else {
	                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	                response.getWriter().write("Session expired or invalid token");
	                return;
	            }
	        }
	    } catch (ExpiredJwtException e) {
	        logger.error("Token expired: {}", e.getMessage());
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        response.getWriter().write("Token has expired. Please log in again.");
	        return;
	    } catch (JwtException e) {  // Generic exception for any JWT-related error
	        logger.error("Invalid token: {}", e.getMessage());
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        response.getWriter().write("Invalid token. Please log in again.");
	        return;
	    } catch (Exception e) {
	        logger.error("Token validation error: {}", e.getMessage());
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().write("An error occurred while processing authentication.");
	        return;
	    }

	    logger.info("Successfully validated the token!");

	    filterChain.doFilter(request, response);
	}


	
}
