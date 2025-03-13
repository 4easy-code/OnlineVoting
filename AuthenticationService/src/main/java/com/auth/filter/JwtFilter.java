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

import io.jsonwebtoken.Claims;
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
	        || path.startsWith("/votingapi/auth/createOtp")
	        || path.startsWith("/votingapi/refresh/createRefreshToken");
	}
	
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		logger.info("Trying to validate the token: {}", request.getHeader("Authorization"));
		try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
                username = jwtUtil.extractUsername(token);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                logger.info("Extracted Role from UserDetails: {}", userDetails.getAuthorities());
                logger.info("Is token validated by JWTUtil: {}", jwtUtil.validateToken(token, userDetails.getUsername()));
                logger.info("Is token validated by Tokenstore: {}", (tokenStore.isTokenValid(username, token) || tokenStore.isRefreshTokenValid(username, token) ));
                logger.info("Username from token: {}", jwtUtil.extractUsername(token));
                
                if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                	Claims claims = jwtUtil.extractAllClaims(token);
                	if(claims == null) {
                		throw new JwtException("Token verification failed: Unable to extract claims.");
                	}
                	String tokenType = claims.get("tokenType", String.class);
                	
                	if("ACCESS".equals(tokenType) && !tokenStore.isTokenValid(username, token)) {
                        logger.warn("Token rejected: Exceeded session limit or logged out.");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Session invalid. Please log in again.");
                        return;
                	} else if ("REFRESH".equals(tokenType)) {
                        // Allow refresh token ONLY for the refresh end point
                        if (!request.getRequestURI().equals("/refresh/createAccessToken")) {
                            logger.warn("Refresh token cannot be used for authentication outside refresh endpoint");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Refresh tokens cannot be used to access protected resources.");
                            return;
                        }
                    } else {
                    	UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    			userDetails,
                    			null,
                    			userDetails.getAuthorities()
                    			);
                    	authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    	SecurityContextHolder.getContext().setAuthentication(authToken);
                    	
                    	logger.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
                    }
                } else {
                	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Session expired or invalid token");
                    return;
                }
            }
            logger.info("successfully validated the token");
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
		
		filterChain.doFilter(request, response);

	}
}
